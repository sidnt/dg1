package messageService

import zio.Chunk
import zio.json._

object domainModels extends App {

  type UID = String
  final case class Message(uid: UID, message: String)
  final case class Messages(messages: List[Message])

  object Message {
    implicit val encoder:JsonEncoder[Message] = DeriveJsonEncoder.gen[Message]
    implicit val decoder:JsonDecoder[Message] = DeriveJsonDecoder.gen[Message]
  }

  object Messages {
    implicit val encoder = DeriveJsonEncoder.gen[Messages]
    implicit val decoder = DeriveJsonDecoder.gen[Messages]
  }

}
