package wechaty

import java.util.concurrent.TimeUnit

import io.github.wechaty.grpc.puppet.Event.EventType
import org.junit.jupiter.api.{Assertions, Test}
import wechaty.puppet.schemas.Event.{EventLoginPayload, EventScanPayload, ScanStatus}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-16
  */
class WechatyEventTest extends TestEventBase {
  @Test
  def test_event: Unit ={
    val payload = new EventScanPayload
    payload.status= ScanStatus.Waiting
    mockEvent( EventType.EVENT_TYPE_SCAN->payload)

    var reach = false
    instance.onScan(f=>{
      reach = true
      Assertions.assertEquals(payload.status,f.status)
    })
    awaitEventCompletion(10,TimeUnit.SECONDS)
    Assertions.assertTrue(reach)
  }
  @Test
  def test_login: Unit ={
    val payload = new EventLoginPayload
    payload.contactId= "contactId"

    var reach = false
    instance.onLogin(f=>{
      reach = true
      Assertions.assertEquals(payload.contactId,f.id)
    })

    mockEvent( EventType.EVENT_TYPE_LOGIN->payload)

    awaitEventCompletion(10,TimeUnit.SECONDS)
    Assertions.assertTrue(reach)
  }
}
