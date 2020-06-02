package wechaty.puppet.schemas

import com.fasterxml.jackson.annotation.{JsonGetter, JsonSetter}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-01
  */
object Events {

  object EventName extends Enumeration {
    type Type = Value
    val PuppetEventNameUnknown: Value = Value("unkown")
    val PuppetEventNameFriendship: Value = Value("friendship")
    val PuppetEventNameLogin: Value = Value("login")
    val PuppetEventNameLogout: Value = Value("logout")
    val PuppetEventNameMessage: Value = Value("message")
    val PuppetEventNameRoomInvite: Value = Value("room-invite")
    val PuppetEventNameRoomJoin: Value = Value("room-join")
    val PuppetEventNameRoomLeave: Value = Value("room-leave")
    val PuppetEventNameRoomTopic: Value = Value("room-topic")
    val PuppetEventNameScan: Value = Value("scan")
    val PuppetEventNameDong: Value = Value("dong")
    val PuppetEventNameError: Value = Value("error")
    val PuppetEventNameHeartbeat: Value = Value("heartbeat")
    val PuppetEventNameReady: Value = Value("ready")
    val PuppetEventNameReset: Value = Value("reset")
    val PuppetEventNameStop: Value = Value("stop")
    val PuppetEventNameStart: Value = Value("start")

    def findEvent(name: String): Value = Value(name)
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
