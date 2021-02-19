package dg
import models._ 
import dgClient._
import com.google.protobuf.ByteString
import io.dgraph._
import DgraphProto._
import zio._

object dgService {

  type DgService = Has[Service]

  private def makeMutationJson(uid: UID, predicate: Predicate, payload: String): String = {
    s"""{"uid": "$uid","$predicate": "$payload"}"""
  }

  private def makeUidQueryJson(uid: UID, predicates: Predicates, queryLabel: String) = {

    val requestedEdges: String = predicates.mkString(",")
  
    s"""
    {
      $queryLabel(func: uid($uid)) {
        $requestedEdges
      }
    }
    """
  }

  private def makeQueryByTermsJson(predicateToMatch: Predicate, terms: String, predicatesToReturn: Predicates, queryLabel: String) = {
    
    val requestedEdges: String = predicatesToReturn.mkString(",")

    s"""
    {
      $queryLabel(func: anyofterms($predicateToMatch, "$terms")) {
        $requestedEdges
      }
    }
    """
  }

  trait Service {
    val dropAll: IO[DgError, Unit]
    def setSchema(schemaString: String): IO[DgError, Unit]
    def mutate(uid: UID, predicate: Predicate, payload: String): IO[DgError, Response]
    def getPredicatesAtUid(uid: UID, predicates: Predicates, queryLabel: String): IO[DgError, String]
    def queryPredicateForTerms(predicateToMatch: Predicate, terms: String, predicatesToReturn: Predicates, queryLabel: String): IO[DgError, String]
    def deletePredicatesAt(uid: UID, predicates: Predicates): IO[DgError, Unit]
  }

  val f: ZIO[DgClient, Nothing, dgService.Service] = for {
    dgClient <- getDgClient
    
    dgService = new Service {

                  val dropAll: IO[DgError, Unit] = for {
                    _         <- Task(dgClient.alter(Operation.newBuilder.setDropAll(true).build)) orElse IO.fail(DgError("Drop All operation failed"))
                  } yield ()

                  def setSchema(schemaString: String): IO[DgError, Unit] = for {
                    _             <-  Task(dgClient.alter(Operation.newBuilder.setSchema(schemaString).build)) orElse IO.fail(DgError("Setting schema failed"))
                  } yield ()

                  def mutate(uid: UID, predicate: Predicate, payload: String): IO[DgError, Response] = for {
                    mutationJson  <-  UIO(makeMutationJson(uid, predicate, payload))
                    mutation      <-  Task(Mutation.newBuilder().setSetJson(ByteString.copyFromUtf8(mutationJson)).build) orElse IO.fail(DgError("mutation building failed"))
                    txn           =   dgClient.newTransaction
                    response      <-  Task({val response = txn.mutate(mutation); txn.commit; response}).ensuring(UIO(txn.discard)) orElse IO.fail(DgError("mutation failed"))
                  } yield response

                  def getPredicatesAtUid(uid: UID, predicates: Predicates, queryLabel: String): IO[DgError, String] = for {
                    queryJson     <-  UIO(makeUidQueryJson(uid, predicates, queryLabel))
                    response      <-  Task(dgClient.newReadOnlyTransaction.query(queryJson)) orElse IO.fail(DgError("readonly transaction failed"))
                    responseJson  =   response.getJson.toStringUtf8
                  } yield responseJson

                  def queryPredicateForTerms(predicateToMatch: Predicate, terms: String, predicatesToReturn: Predicates, queryLabel: String): IO[DgError, String] = for {
                    queryJson     <-  UIO(makeQueryByTermsJson(predicateToMatch, terms, predicatesToReturn, queryLabel))
                    response      <-  Task(dgClient.newReadOnlyTransaction.query(queryJson)) orElse IO.fail(DgError("query failed"))
                    responseJson  =   response.getJson.toStringUtf8
                  } yield responseJson

                  def deletePredicatesAt(uid: UID, predicates: Predicates): IO[DgError, Unit] = for {
                    mutation      <-  Task {
                                        var mu = Mutation.newBuilder().build()
                                        mu = Helpers.deleteEdges(mu, uid, predicates: _*);
                                        mu
                                      } orElse IO.fail(DgError("deletePredicates mutation building failed"))
                    txn           =   dgClient.newTransaction
                    _             <-  (Task{txn.mutate(mutation); txn.commit}).ensuring(UIO(txn.discard)) orElse IO.fail(DgError("mutation failed"))
                  } yield ()

                }

  } yield dgService

  val localLiveDgService: ZLayer[Any, DgError, DgService] = localLiveDgClient >>> ZLayer.fromEffect(f)
  val accessDgService = ZIO.access[DgService](_.get)

}
