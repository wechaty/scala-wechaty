package wechaty

import java.util.concurrent.{CountDownLatch, TimeUnit}

import io.github.wechaty.grpc.PuppetGrpc
import io.github.wechaty.grpc.puppet.Base.{LogoutResponse, StartResponse, StopResponse}
import io.github.wechaty.grpc.puppet.Event.{EventResponse, EventType}
import io.github.wechaty.grpc.puppet.Message.{MessagePayloadResponse, MessageType}
import io.grpc.ManagedChannelBuilder
import org.grpcmock.GrpcMock
import org.grpcmock.GrpcMock.{serverStreamingMethod, stubFor, unaryMethod}
import org.grpcmock.junit5.GrpcMockExtension
import org.junit.jupiter.api.{AfterEach, BeforeEach}
import org.junit.jupiter.api.extension.ExtendWith
import wechaty.Wechaty.PuppetResolver
import wechaty.puppet.schemas.Event.EventResetPayload
import wechaty.puppet.schemas.Puppet
import wechaty.puppet.schemas.Puppet.PuppetOptions

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-07
  */
@ExtendWith(Array(classOf[GrpcMockExtension]))
class TestBase {
  protected var instance:Wechaty = null

  @BeforeEach
  def setupChannel(): Unit = {
    countDownLatch = new CountDownLatch(1)
    val serverChannel = ManagedChannelBuilder.forAddress("localhost", GrpcMock.getGlobalPort).usePlaintext.build
    //for server stub
    val eventResponse = EventResponse.newBuilder().build()
    stubFor(unaryMethod(PuppetGrpc.getEventMethod).willReturn(eventResponse))
    val startResponse = StartResponse.newBuilder().build()
    stubFor(unaryMethod(PuppetGrpc.getStartMethod).willReturn(startResponse))
    val logoutResponse = LogoutResponse.newBuilder().build()
    stubFor(unaryMethod(PuppetGrpc.getLogoutMethod).willReturn(logoutResponse))

    val stopResponse = StopResponse.newBuilder().build()
    stubFor(unaryMethod(PuppetGrpc.getStopMethod).willReturn(stopResponse))

    val wechatyOptions = new WechatyOptions
    val options = new PuppetOptions
    options.endPoint = Some("localhost:1234")//avoid to fetch api server
    options.channelOpt = Some(serverChannel) //using test channel
    wechatyOptions.puppetOptions = Some(options)
    instance = Wechaty.instance(wechatyOptions)
    instance.start()
  }
  @AfterEach
  def stopInstance: Unit ={
    instance.stop()
  }
  protected implicit lazy val puppetResolver: PuppetResolver = {
    instance
  }
  protected def startWithEvent(): Unit ={
    instance.start()
    instance.onReset(payload=>{
      countDownLatch.countDown()
    })
  }

  var countDownLatch = new CountDownLatch(1)
  protected def awaitEventCompletion(time:Long,unit:TimeUnit): Unit ={
    countDownLatch.await(time,unit)
    countDownLatch = new CountDownLatch(1)
  }
  private var isMockEvented = false
  protected def mockEvent(responses:(EventType,AnyRef)*): Unit ={
    if(isMockEvented) throw new IllegalStateException("mock event must be called only one time!")
    val resetPayload = new EventResetPayload
    val tmpResponses = responses.toList :+ (EventType.EVENT_TYPE_RESET ->resetPayload)
    val eventResponses = tmpResponses.map{case (event,payload) =>
        val eventResponse = EventResponse.newBuilder()
        eventResponse.setType(event)
        eventResponse.setPayload(Puppet.objectMapper.writeValueAsString(payload))
        eventResponse.build()
    }

    stubFor(serverStreamingMethod(PuppetGrpc.getEventMethod()).willReturn(eventResponses:_*))

    startWithEvent()
    isMockEvented = true
  }
}
