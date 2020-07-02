package wechaty.plugins

import io.github.wechaty.grpc.PuppetGrpc
import org.grpcmock.GrpcMock.{calledMethod, times, verifyThat}
import org.junit.jupiter.api.Test
import wechaty.TestBase
import wechaty.puppet.schemas.Event.EventRoomJoinPayload
import wechaty.puppet.schemas.Puppet.PuppetEventName

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-23
  */
class RoomJoinHelloTest extends TestBase{
  @Test
  def test_join: Unit ={
    val roomJoinHello = new RoomJoinHello(RoomJoinHelloConfig(rooms = Array("roomId"),hello = "hello world!"),isWait=true)
    roomJoinHello.install(instance)

    mockRoomMessage(roomId = "from")
    mockMessageSendText()
    emitMessagePayloadEvent("messageId")

    mockContactPayload("jcai")
    val payload=new EventRoomJoinPayload
    payload.roomId="roomId"
    payload.inviteeIdList=Array()
    instance.puppet.emit(PuppetEventName.ROOM_JOIN,payload)

    verifyThat(
      calledMethod(PuppetGrpc.getMessageSendTextMethod),
      times(0));


    payload.inviteeIdList=Array("jcai")
    instance.puppet.emit(PuppetEventName.ROOM_JOIN,payload)

    verifyThat(
      calledMethod(PuppetGrpc.getMessageSendTextMethod),
      times(1));

  }
}
