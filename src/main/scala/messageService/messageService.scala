package messageService

import zio._

import dg.models._
import domainModels._
import dg.dgService._
import dg.models
import io.dgraph.DgraphProto._
import scala.jdk.CollectionConverters._
import zio.json._

object messageService {

  type MessageService = Has[messageService.Service]

  trait Service {

    def getMessage(uid: UID): IO[DgError, Message]
    def messageExists(uid: UID): IO[DgError, Boolean]
    def storeMessage(message: String): IO[DgError, UID]
    def updateMessage(uid: UID, newMessage: String): IO[DgError, Unit]
    def deleteMessage(uid: UID): IO[DgError, Unit]
    def findMessages(terms: String): IO[DgError, Messages]

  }

  val f = for {

    dgService   <-  accessDgService

    msgService  =   new Service {

                      def getMessage(uid: UID): IO[DgError, Message] = for {
                        responseJson  <-  dgService.getPredicatesAtUid(uid, List("uid","message"), "messages")
                        message     <-   ZIO.fromEither(responseJson.fromJson[Messages]).map(m => m.messages(0)) orElse IO.fail(DgError("getting message failed"))
                      } yield message
                      
                      def messageExists(uid: UID): IO[DgError, Boolean] = for {
                        responseJson  <-  dgService.getPredicatesAtUid(uid, List("uid","message"), "messages")
                        canDecode     =   responseJson.fromJson[Messages]
                        exists        =   canDecode.fold(_ => false, _ => true)
                      } yield exists
                      
                      def storeMessage(message: String): IO[DgError, UID] = for {
                        mutationResponse     <- dgService.mutate("_:blank", "message", message) orElse IO.fail(DgError("storing message failed"))
                      } yield mutationResponse.getUidsMap.asScala("blank")

                      def updateMessage(uid: UID, newMessage: String): IO[DgError, Unit] = for {
                        exists    <-    messageExists(uid)
                        _         <-    if (exists) dgService.mutate(uid, "message", newMessage) else IO.fail(DgError("message doesn't already exist"))
                      } yield ()

                      def findMessages(terms: String): IO[DgError, Messages] = for {
                        responseJson    <-    dgService.queryPredicateForTerms("message", terms, List("uid","message"), "messages")
                        messages        <-    ZIO.fromEither(responseJson.fromJson[Messages]).mapError(e => DgError(e))
                      } yield messages

                      def deleteMessage(uid: UID): IO[DgError, Unit] = for {
                        _       <-  dgService.deletePredicatesAt(uid: UID, List("message"))
                      } yield ()

                    }
  } yield msgService

  val accessMessageService = ZIO.access[MessageService](_.get)
  val localLiveMessageService = localLiveDgService >>> ZLayer.fromEffect(f)
} 
