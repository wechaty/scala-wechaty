package wechaty.puppet.schemas

import com.fasterxml.jackson.annotation.{JsonGetter, JsonSetter}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-01
  */
object Events {

  object PuppetEventName extends Enumeration {
    type Type = Value
    val UNKNOWN: Value = Value(0)
    val FRIENDSHIP: Value = Value(1)
    val LOGIN: Value = Value(2)
    val LOGOUT: Value = Value(3)
    val MESSAGE: Value = Value(4)
    val INVITE: Value = Value(5)
    val ROOM_JOIN: Value = Value(6)
    val ROOM_LEAVE: Value = Value(7)
    val ROOM_TOPIC: Value = Value(8)
    val SCAN: Value = Value(9)
    val DONG: Value = Value(10)
    val ERROR: Value = Value(11)
    val HEARTBEAT: Value = Value(12)
    val READY: Value = Value(13)
    val RESET: Value = Value(14)
    val STOP: Value = Value(15)
    val START: Value = Value(16)
  }

  object ScanStatus extends Enumeration {
    type Type = Value
    val UNKNOWN: Value = Value(-1)
    val CANCEL: Value = Value(0)
    val WAITING: Value = Value(1)
    val SCANNED: Value = Value(2)
    val CONFIRMED: Value = Value(3)
    val TIMEOUT: Value = Value(4)
  }

  sealed class EventPayload

  class EventFriendshipPayload extends EventPayload {
    var friendshipID: String = _
  }

  class EventLoginPayload extends EventPayload {
    var contactId: String = _
  }

  class EventLogoutPayload extends EventPayload {
    var contactId: String = _
    var data: String = _
  }

  class EventMessagePayload extends EventPayload {
    var messageId: String = _
  }

  class EventRoomInvitePayload extends EventPayload {
    var roomInvitationId: String = _
  }

  class EventRoomJoinPayload extends EventPayload {
    var inviteeIdList: Array[String] = _
    var inviterId: String = _
    var roomId: String = _
    var timestamp: Long = _
  }

  class EventRoomLeavePayload extends EventPayload {
    var removeeIdList: Array[String] = _
    var removerId: String = _
    var roomId: String = _
    var timestamp: Long = _
  }

  class EventRoomTopicPayload extends EventPayload {
    var changerId: String = _
    var newTopic: String = _
    var oldTopic: String = _
    var roomId: String = _
    var timestamp: Long = _
  }

  class EventScanPayload extends BaseEventPayload {
    @JsonGetter
    var status: ScanStatus.Type = _
    var qrcode: String = _
    @JsonSetter
    def setStatus(status:Int): Unit ={
      this.status = ScanStatus(status)
    }
  }

  class BaseEventPayload extends EventPayload {
    var data: String = _
  }

  type EventDongPayload = BaseEventPayload

  type EventErrorPayload = BaseEventPayload

  type EventReadyPayload = BaseEventPayload

  type EventResetPayload = BaseEventPayload

  type EventHeartbeatPayload = BaseEventPayload

}
