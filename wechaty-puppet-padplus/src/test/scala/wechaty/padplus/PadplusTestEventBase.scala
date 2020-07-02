package wechaty.padplus

import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.typesafe.scalalogging.LazyLogging
import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import org.grpcmock.GrpcMock
import org.grpcmock.GrpcMock._
import org.grpcmock.junit5.GrpcMockExtension
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.{AfterEach, BeforeEach}
import wechaty.padplus.grpc.PadPlusServerGrpc
import wechaty.padplus.grpc.PadPlusServerOuterClass._
import wechaty.puppet.schemas.Event.EventResetPayload
import wechaty.puppet.schemas.Puppet
import wechaty.puppet.schemas.Puppet.{PuppetEventName, PuppetOptions}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-07-01
  */
@ExtendWith(Array(classOf[GrpcMockExtension]))
class PadplusTestEventBase extends LazyLogging{
  private var serverChannel: ManagedChannel = _
  private var isMocked= false

  var instance:PuppetPadplus = _
  var uin="1234567"

  @BeforeEach
  def setupChannel(): Unit = {
    isMocked = false
    countDownLatch = new CountDownLatch(1)
    serverChannel = ManagedChannelBuilder.forAddress("localhost", GrpcMock.getGlobalPort).usePlaintext.build

    val options = new PuppetOptions
    options.endPoint = Some("localhost:1234")//avoid to fetch api server
    options.channelOpt = Some(serverChannel) //using test channel
    options.token=Some("token")

    instance = new PuppetPadplus(options){
      override protected def generateTraceId(apiType: ApiType): String = {
        apiType match{
          case ApiType.INIT | ApiType.HEARTBEAT => apiType.getNumber.toString
          case ApiType.GET_CONTACT => ResponseType.CONTACT_SEARCH.getNumber.toString
          case _ =>
            apiType.getNumber.toString
//            super.generateTraceId(apiType)
        }
      }
      override protected def getUin: Option[String] = {
        Some(uin)
      }
    }

    val requestBuilder = RequestObject.newBuilder().setApiType(ApiType.HEARTBEAT)
      .setToken("token").setTraceId(ApiType.HEARTBEAT.getNumber.toString)
    val responseBuilder = ResponseObject.newBuilder().setResult("success")
    stubFor(unaryMethod(PadPlusServerGrpc.getRequestMethod)
      .withRequest(requestBuilder.build())
      .willReturn(responseBuilder.build())
    )
val requestBuilder2 = RequestObject.newBuilder()
  .setUin(uin)
  .setApiType(ApiType.INIT)
  .setToken("token").setTraceId(ApiType.INIT.getNumber.toString)
    stubFor(unaryMethod(PadPlusServerGrpc.getRequestMethod)
      .withRequest(requestBuilder2.build())
      .willReturn(responseBuilder.build())
    )

    instance.startGrpc()
  }
  @AfterEach
  def stop: Unit ={
    instance.stop()
  }
  protected def startWithEvent(): Unit ={
    instance.addListener[EventResetPayload](PuppetEventName.RESET,v=>{
      countDownLatch.countDown()
    })
    instance.startAwaitStream()
  }


  var countDownLatch:CountDownLatch = _
  protected def awaitEventCompletion(): Unit ={
    countDownLatch.await(2,TimeUnit.SECONDS)
  }

  protected def mockEvent(responses:(ResponseType,AnyRef)*): Unit ={
    if(isMocked) throw new IllegalStateException("mock event must be called only one time!")
    val disconnectPayload:Option[String] = None
    val tmpResponses = responses.toList :+ (ResponseType.DISCONNECT->disconnectPayload)
    val eventResponses = tmpResponses.map{case (event,payload) =>
      val eventResponse = StreamResponse.newBuilder()
      eventResponse.setResponseType(event)
      eventResponse.setTraceId(event.getNumber.toString)
      eventResponse.setData(Puppet.objectMapper.writeValueAsString(payload))
      eventResponse.build()
    }

    stubFor(serverStreamingMethod(PadPlusServerGrpc.getInitMethod)
      .willReturn(eventResponses:_*))

    startWithEvent()
    isMocked = true
    awaitEventCompletion()
  }

}
