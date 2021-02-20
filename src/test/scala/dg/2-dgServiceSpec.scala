package dg

import models._

import zio._
import zio.test._
import zio.test.Assertion._

import dgService._
import io.dgraph.DgraphProto
import scala.jdk.CollectionConverters._

object dgServiceSpec {

  val dgServiceSuite = /**suite("dgServiceSpec")(*/
    testM("mutate & getPredicatesAtUid & queryPredicateForTerms works") {
      (for {
        dgService     <-    accessDgService
        _             <-    dgService.dropAll
        _             <-    dgService.setSchema("is: string @index(term) .")
        payloadIn     =     "a simple payload"
        mutResponse   <-    dgService.mutate("_:blank", "is", payloadIn)
        allocatedUid  =     mutResponse.getUids.asScala("blank")
        responseJson1 <-    dgService.getPredicatesAtUid(allocatedUid, List("is"), "uidQueryResult")
        responseJson2 <-    dgService.queryPredicateForTerms("is", "payload", List("is"), "queryPredicateResult")
        payloadOut1   =     responseJson1.split(':')(2).substring(1, payloadIn.length+1)
        payloadOut2   =     responseJson2.split(':')(2).substring(1, payloadIn.length+1)
        assertResult  =     payloadIn == payloadOut1 && payloadIn == payloadOut2
        _             <-    dgService.dropAll
      } yield assert(assertResult)(equalTo(true))).provideCustomLayer(localLiveDgService)
    }
//  )
}
