package wechaty.padplus

import java.io.File
import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.typesafe.scalalogging.LazyLogging
import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import org.apache.commons.io.FileUtils
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
    val storePath="/tmp/store"
    FileUtils.deleteQuietly(new File(storePath))
    isMocked = false
    countDownLatch = new CountDownLatch(1)
    serverChannel = ManagedChannelBuilder.forAddress("localhost", GrpcMock.getGlobalPort).usePlaintext.build

    val options = new PuppetOptions
    options.endPoint = Some("localhost:1234")//avoid to fetch api server
    options.channelOpt = Some(serverChannel) //using test channel
    options.token=Some("token")

    instance = new PuppetPadplus(options,storePath){
      override protected def generateTraceId(apiType: ApiType): String = {
        NEED_CALLBACK_API_LIST.values.find( _ == apiType) match{
          case Some(v) =>
            /** 使用apiType作为Traceid,方便进行测试*/
            v.getNumber.toString
          case _ =>
            super.generateTraceId(apiType)
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

      NEED_CALLBACK_API_LIST.get(event) match{
        case Some(apiEvent) =>
          eventResponse.setTraceId(apiEvent.getNumber.toString)
        case _ =>

      }

      eventResponse.setData(Puppet.objectMapper.writeValueAsString(payload))
      eventResponse.build()
    }

    stubFor(serverStreamingMethod(PadPlusServerGrpc.getInitMethod)
      .willReturn(eventResponses:_*))

    startWithEvent()
    isMocked = true
    awaitEventCompletion()
  }
  private val NEED_CALLBACK_API_LIST=Map(
//    ApiType.INIT,
//    ApiType.SEND_MESSAGE,
//    ApiType.SEND_FILE,
//    ApiType.GET_MESSAGE_MEDIA,
//    ApiType.SEARCH_CONTACT,
//    ApiType.ADD_CONTACT,
//    ApiType.CREATE_ROOM,
//    ApiType.GET_ROOM_ANNOUNCEMENT,
//    ApiType.SET_ROOM_ANNOUNCEMENT,
//    ApiType.HEARTBEAT,
//    ApiType.CREATE_TAG,
//    ApiType.ADD_TAG,
//    ApiType.MODIFY_TAG,
//    ApiType.DELETE_TAG,
//    ApiType.GET_ALL_TAG,
//    ApiType.GET_ROOM_QRCODE,
//    ApiType.GET_CONTACT_SELF_QRCODE,
//    ApiType.SET_CONTACT_SELF_INFO,
    ResponseType.CONTACT_SELF_INFO_GET -> ApiType.GET_CONTACT_SELF_INFO
//    ApiType.LOGOUT,
//    ApiType.REVOKE_MESSAGE,
//    ApiType.ACCEPT_ROOM_INVITATION,
//    ApiType.LOGIN_DEVICE
  )

}
