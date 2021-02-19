package dg

import models._

import zio._
import zio.test._
import zio.test.Assertion._

import dgService._
import io.dgraph.DgraphProto
import scala.jdk.CollectionConverters._

object dgServiceSpec extends DefaultRunnableSpec {

  def spec = suite("dgServiceSpec")(
    testM("mutate & get predicatesAtUid works") {
      (for {
        dgService     <-    accessDgService
        _             <-    dgService.dropAll
        _             <-    dgService.setSchema("is: string .")
        payloadIn     =     "a simple payload"
        mutResponse   <-    dgService.mutate("_:blank", "is", payloadIn)
        allocatedUid  =     mutResponse.getUids.asScala("blank")
        responseJson  <-    dgService.getPredicatesAtUid(allocatedUid, List("is"), "uidQueryResult")
        payloadOut    =     responseJson.split(':')(2).substring(1, payloadIn.length+1)
        // _             <-    console.putStrLn(payloadOut)
      } yield assert(payloadIn)(equalTo(payloadOut))).provideCustomLayer(localLiveDgService)
    }
  )
}
