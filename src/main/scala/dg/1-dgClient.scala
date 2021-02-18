package dg

import zio._
import io.grpc._
import io.dgraph._

import models._
import dgConfig._

object dgClient {

  type DgClient = Has[Service]

  private [dgClient] trait Service {
    val dgClient: DgraphClient
    val grpcChannel: ManagedChannel
  }

  val getDgClient = ZIO.access[DgClient](_.get.dgClient)

  val f: ZManaged[DgConfig, DgError, dgClient.Service] = ZManaged.make(
    
    for {
      dgConfigS     <-  ZIO.access[DgConfig](_.get)
      grpcChannel1  <-  Task(ManagedChannelBuilder.forAddress(dgConfigS.host, dgConfigS.port).usePlaintext.build) orElse IO.fail(DgError("managed channel building failed"))
      dgClient1     <-  Task(new DgraphClient(DgraphGrpc.newStub(grpcChannel1))) orElse IO.fail(DgError("dgraph client creation failed"))
      dgClientS     =   new Service {
                          val dgClient = dgClient1
                          val grpcChannel = grpcChannel1
                        }
    } yield dgClientS
  )(
    dgClientS => for {
      _ <- UIO(dgClientS.grpcChannel.shutdown())
    } yield ()
  )

  val localLiveDgClient: ZLayer[Any, DgError, DgClient] = localDgConfig >>> ZLayer.fromManaged(f)

}
