import zio.test.DefaultRunnableSpec
import messageService.messageServiceSpec._
import messageService.domainModelSpec._
import dg.dgServiceSpec._
import dg.dgClientSpec._
import zio.test.TestAspect._

object masterSuite extends DefaultRunnableSpec {

  def spec = suite("master suite")(domainModelSpec, dgClientSpec, dgServiceSuite, messageServiceSpec) @@ sequential

}