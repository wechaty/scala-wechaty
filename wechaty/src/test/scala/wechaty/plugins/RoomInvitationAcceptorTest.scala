package wechaty.plugins

import java.util.concurrent.TimeUnit

import io.github.wechaty.grpc.PuppetGrpc
import io.github.wechaty.grpc.puppet.Event.EventType
import io.github.wechaty.grpc.puppet.RoomInvitation.RoomInvitationAcceptResponse
import org.grpcmock.GrpcMock.{calledMethod, stubFor, times, unaryMethod, verifyThat}
import org.junit.jupiter.api.Test
import wechaty.TestEventBase
import wechaty.puppet.schemas.Event.EventRoomInvitePayload

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-28
  */
class RoomInvitationAcceptorTest extends TestEventBase{
  @Test
  def test_invite: Unit ={
    val roomInvitationAcceptResponse = RoomInvitationAcceptResponse.newBuilder().build()
    stubFor(unaryMethod(PuppetGrpc.getRoomInvitationAcceptMethod)
      .willReturn(roomInvitationAcceptResponse))

    instance.use(new RoomInvitationAcceptor)
    val payload = new EventRoomInvitePayload
    payload.roomInvitationId ="roomid"
    mockEvent(EventType.EVENT_TYPE_ROOM_INVITE ->payload)
    awaitEventCompletion(10,TimeUnit.SECONDS)

    verifyThat(
      calledMethod(PuppetGrpc.getRoomInvitationAcceptMethod),
      times(1));

  }

}
