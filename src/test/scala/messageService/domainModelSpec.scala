package messageService

import domainModels._
import zio.json._
import zio.test._
import zio.test.Assertion._

object domainModelSpec {
  def domainModelSpec = suite("domain model codec tests")(

    test("json encoding / decoding test for Message")(assert {
      Message("0x1","hello").toJson
    }(equalTo("""{"uid":"0x1","message":"hello"}"""))),

    test("json encoding / decoding test for Messages")(assert{
      Messages(List(Message("0x1","hello"),Message("0x2","world"))).toJson
    }(equalTo("""{"messages":[{"uid":"0x1","message":"hello"},{"uid":"0x2","message":"world"}]}""")))
  )
}
