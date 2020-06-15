package wechaty.user

import io.github.wechaty.grpc.PuppetGrpc
import io.github.wechaty.grpc.puppet.Room.RoomPayloadResponse
import org.grpcmock.GrpcMock.{stubFor, unaryMethod}
import org.junit.jupiter.api.{Assertions, Test}
import wechaty.TestBase
import wechaty.puppet.schemas.Event.EventRoomJoinPayload
import wechaty.puppet.schemas.Puppet.PuppetEventName

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-15
  */
class RoomTest extends TestBase{
  @Test
  def testEvent: Unit ={

    val roomId = "roomIdxxxx"
    val contactId="id1"
    val inviterId="inviterId"
    val response = RoomPayloadResponse.newBuilder()
    response.setId(roomId)
    stubFor(unaryMethod(PuppetGrpc.getRoomPayloadMethod)
      .willReturn(response.build())
    )


    val room = Room.load(roomId)(instance).get
    var reachFlag = false
    room.onJoin({case (list,inviter,date)=>
      reachFlag = true
      Assertions.assertEquals(inviterId,inviter.id)
    })

    val payload = new EventRoomJoinPayload
    payload.inviteeIdList = Array(contactId)
    payload.inviterId = inviterId
    payload.roomId = roomId
    instance.puppet.emit(PuppetEventName.ROOM_JOIN,payload)

    Assertions.assertTrue(reachFlag)
  }
}
