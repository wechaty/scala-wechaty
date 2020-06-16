package wechaty

import java.util.concurrent.TimeUnit

import io.github.wechaty.grpc.puppet.Event.EventType
import org.junit.jupiter.api.{Assertions, Test}
import wechaty.puppet.schemas.Event.EventFriendshipPayload

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-16
  */
class WechatyTest extends TestBase {
  @Test
  def test_friendship: Unit ={
    val payload = new EventFriendshipPayload
    payload.friendshipId="fid"
    mockEvent( EventType.EVENT_TYPE_FRIENDSHIP->payload)

    var reach = false
    instance.onFriendAdd(f=>{
      reach = true
      Assertions.assertEquals(payload.friendshipId,f.id)
    })

//  instance.puppet.emit(PuppetEventName.FRIENDSHIP,payload)
    awaitEventCompletion(10,TimeUnit.SECONDS)
    Assertions.assertTrue(reach)
  }

}
