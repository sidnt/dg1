package dg

import zio.Has
import zio.ZLayer

object dgConfig {

  type DgConfig = Has[dgConfig.Service]

  trait Service {
    def host: String
    def port: Int
  }

  val localDgConfig: ZLayer[Any, Nothing, DgConfig] = ZLayer.succeed(
    new Service {
      def host: String = "localhost"
      def port: Int = 9080
    }
  )
  
}
