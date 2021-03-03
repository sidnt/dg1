package dg

import dgClient._
import dgConfig.localDgConfig

import zio._
import zio.test._
import zio.test.Assertion._

object dgClientSpec {
  def dgClientSpec = suite("dgClientSpec")(
    testM("can check dg version if local dg instance is up") {
      for {
        dgClient <- getDgClient //.provideLayer(localLiveDgClient) [1]
        dgVersion <- Task(dgClient.checkVersion.getTag)
        // dgVersion <- Task(throw new Exception("boom")) [2]
      } yield assert(dgVersion)(equalTo("v20.11.1"))
    }
  )
}

/** [1] #quiz why does this not work
  * localLiveDgClient is a managed layer
  * as soon as it exits the scope, the managed channel will be closed
  * and checking the version after it has been closed
  * will result in error
  * so we have to provide the layer right down there
  */

/** [2] if this happens,
  * the test will also fail
  * and you will get a stacktrace in console as well
  */
