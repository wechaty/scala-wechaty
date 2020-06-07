package wechaty

import io.github.wechaty.grpc.PuppetGrpc
import io.github.wechaty.grpc.puppet.Base.{LogoutResponse, StartResponse}
import io.github.wechaty.grpc.puppet.Event.EventResponse
import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import org.grpcmock.GrpcMock
import org.grpcmock.GrpcMock.{stubFor, unaryMethod}
import org.grpcmock.junit5.GrpcMockExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import wechaty.Wechaty.PuppetResolver
import wechaty.hostie.PuppetHostie
import wechaty.puppet.Puppet
import wechaty.puppet.schemas.Puppet.PuppetOptions

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-07
  */
@ExtendWith(Array(classOf[GrpcMockExtension]))
class TestBase {
  private var serverChannel:ManagedChannel = null

  @BeforeEach
  def setupChannel(): Unit = {
    serverChannel = ManagedChannelBuilder.forAddress("localhost", GrpcMock.getGlobalPort).usePlaintext.build
  }
  protected implicit  val puppetResolver: PuppetResolver = new PuppetResolver {
    //for server stub
    val eventResponse = EventResponse.newBuilder().build()
    stubFor(unaryMethod(PuppetGrpc.getEventMethod).willReturn(eventResponse))
    val startResponse = StartResponse.newBuilder().build()
    stubFor(unaryMethod(PuppetGrpc.getStartMethod).willReturn(startResponse))
    val logoutResponse = LogoutResponse.newBuilder().build()
    stubFor(unaryMethod(PuppetGrpc.getLogoutMethod).willReturn(logoutResponse))

    override def puppet: Puppet = {
      val options = new PuppetOptions
      options.endPoint=Some("localhost:1234")
      val p = new PuppetHostie(options){
        override protected def initChannel(endpoint: String): Unit = {
          this.channel = serverChannel
        }
      }
      p.start()
      p
    }
  }

}
