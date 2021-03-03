import dg.dgClient.localLiveDgClient
import zio.test.{DefaultRunnableSpec, TestFailure}
import messageService.messageServiceSpec._
import messageService.domainModelSpec._
import dg.dgServiceSpec._
import dg.dgClientSpec._
import dg.dgService.localLiveDgService
import messageService.messageService.localLiveMessageService
import zio.test.TestAspect._

object masterSuite extends DefaultRunnableSpec {

  def spec =
    suite("master suite")(
      domainModelSpec,
      dgClientSpec,
      dgServiceSuite,
      messageServiceSpec
    ).provideLayerShared(
      (localLiveDgService ++ localLiveDgClient ++ localLiveMessageService).mapError(TestFailure.fail)
    ) @@ sequential

}