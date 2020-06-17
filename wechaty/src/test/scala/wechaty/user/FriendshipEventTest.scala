package wechaty.user

import java.util.concurrent.TimeUnit

import io.github.wechaty.grpc.PuppetGrpc
import io.github.wechaty.grpc.puppet.Event.EventType
import io.github.wechaty.grpc.puppet.Friendship.{FriendshipAcceptResponse, FriendshipPayloadResponse, FriendshipType}
import org.grpcmock.GrpcMock.{stubFor, unaryMethod}
import org.junit.jupiter.api.{Assertions, Test}
import wechaty.TestEventBase
import wechaty.puppet.schemas.Event.EventFriendshipPayload

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-17
  */
class FriendshipEventTest extends TestEventBase{
  @Test
  def test_event: Unit ={
    val payload = new EventFriendshipPayload
    payload.friendshipId="fid"

    val response = FriendshipPayloadResponse.newBuilder()
      .setContactId("contactId")
      .setType(FriendshipType.FRIENDSHIP_TYPE_RECEIVE)
      .build()
    stubFor(unaryMethod(PuppetGrpc.getFriendshipPayloadMethod)
      .willReturn(response))

    val friendshipAccept= FriendshipAcceptResponse.newBuilder()
      .build()
    stubFor(unaryMethod(PuppetGrpc.getFrendshipAcceptMethod)
      .willReturn(friendshipAccept))


    var reach = false
    instance.onFriendAdd(f=>{
      reach = true
      Assertions.assertEquals(payload.friendshipId,f.id)
      f.accept()
    })
    mockEvent(EventType.EVENT_TYPE_FRIENDSHIP->payload)

    awaitEventCompletion(10,TimeUnit.SECONDS)
    Assertions.assertTrue(reach)
  }
}
