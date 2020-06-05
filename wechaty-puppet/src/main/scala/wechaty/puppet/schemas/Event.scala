package wechaty.puppet.schemas


object Event {

  /**
    * The event `scan` status number.
    */

  object ScanStatus extends Enumeration {
    type Type = Value
    val Unknown: Type = Value(0)
    val Cancel: Type = Value(1)
    val Waiting: Type = Value(2)
    val Scanned: Type = Value(3)
    val Confirmed: Type = Value(4)
    val Timeout: Type = Value(5)
  }

  sealed trait EventPayload

  class EventFriendshipPayload extends EventPayload {
    var friendshipId: String = _
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
    var timestamp: Number = _
  }

  class EventRoomLeavePayload extends EventPayload {
    var removeeIdList: Array[String] = _
    var removerId: String = _
    var roomId: String = _
    var timestamp: Number = _
  }

  class EventRoomTopicPayload extends EventPayload {
    var changerId: String = _
    var newTopic: String = _
    var oldTopic: String = _
    var roomId: String = _
    var timestamp: Number = _
  }

  class EventScanPayload extends EventPayload {
    var status: ScanStatus.Type = _

    var qrcode: String = _
    var data: String = _
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
