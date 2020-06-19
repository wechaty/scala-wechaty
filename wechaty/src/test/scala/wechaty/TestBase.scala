package wechaty

import java.util.concurrent.{CountDownLatch, TimeUnit}

import io.github.wechaty.grpc.PuppetGrpc
import io.github.wechaty.grpc.puppet.Base.{LogoutResponse, StartResponse, StopResponse}
import io.github.wechaty.grpc.puppet.Contact.ContactPayloadResponse
import io.github.wechaty.grpc.puppet.Event.{EventResponse, EventType}
import io.github.wechaty.grpc.puppet.Message.{MessagePayloadResponse, MessageSendTextResponse, MessageType}
import io.github.wechaty.grpc.puppet.Room.RoomPayloadResponse
import io.github.wechaty.grpc.puppet.RoomMember.RoomMemberPayloadResponse
import io.grpc.ManagedChannelBuilder
import org.grpcmock.GrpcMock
import org.grpcmock.GrpcMock.{serverStreamingMethod, stubFor, unaryMethod}
import org.grpcmock.junit5.GrpcMockExtension
import org.junit.jupiter.api.{AfterEach, BeforeEach}
import org.junit.jupiter.api.extension.ExtendWith
import wechaty.Wechaty.PuppetResolver
import wechaty.hostie.PuppetHostie
import wechaty.puppet.schemas.Event.{EventMessagePayload, EventResetPayload}
import wechaty.puppet.schemas.Puppet
import wechaty.puppet.schemas.Puppet.{PuppetEventName, PuppetOptions}

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
    GrpcMock.resetMappings()
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
    instance.puppet.asInstanceOf[PuppetHostie].idOpt=Some("me")
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
  protected def mockRoomMessage(message:String="message",roomId:String="roomId",memberIds:Array[String]=Array()): Unit ={
    val response = MessagePayloadResponse.newBuilder()
      .setText(message)
      .setType(MessageType.MESSAGE_TYPE_VIDEO) //TODO because RPC return wrong messageType
      .setRoomId(roomId)
      .build()
    stubFor(unaryMethod(PuppetGrpc.getMessagePayloadMethod)
      .willReturn(response))

    val roomPayloadResponse = RoomPayloadResponse.newBuilder()
    roomPayloadResponse.setId(roomId)
    stubFor(unaryMethod(PuppetGrpc.getRoomPayloadMethod)
      .willReturn(roomPayloadResponse.build()))

    val roomMemberPayloadResponse = RoomMemberPayloadResponse.newBuilder().build()
    stubFor(unaryMethod(PuppetGrpc.getRoomMemberPayloadMethod)
      .willReturn(roomMemberPayloadResponse)
    )

    memberIds.toList match{
      case head::remain=>
        val method = unaryMethod(PuppetGrpc.getContactPayloadMethod)
        val response = ContactPayloadResponse.newBuilder()
          .setName(head)
          .build()
        val next = method.willReturn(response)
        remain.foldLeft(next){(n,id)=>{
          val response = ContactPayloadResponse.newBuilder()
            .setName(id)
            .build()
          n.nextWillReturn(response)
        }}
        stubFor(method)
      case Nil =>
    }



  }
  protected def mockMessageSendText(): Unit ={
    val response = MessageSendTextResponse.newBuilder()
      .build()
    stubFor(unaryMethod(PuppetGrpc.getMessageSendTextMethod)
      .willReturn(response))
  }
  protected def mockMessagePayloadEvent(messageId:String="messageId"): Unit ={
    val payload=new EventMessagePayload
    payload.messageId=messageId
    instance.puppet.emit(PuppetEventName.MESSAGE,payload)
  }
  protected def resetGrpcMock(): Unit ={
    GrpcMock.resetMappings()
  }
}
