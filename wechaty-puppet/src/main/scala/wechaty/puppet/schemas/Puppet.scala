package wechaty.puppet.schemas

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.github.wechaty.grpc.puppet.Event.EventType
import wechaty.puppet.schemas.Events.EventName

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-01
  */
object Puppet {
  class PuppetOptions {
    var endPoint: Option[String] = None
    var timeout: Option[Long] = None
    var token: Option[String] = None
    var puppetOptionKey: Option[String] = None
  }
  lazy val objectMapper = {
    val om = new ObjectMapper()
    om.registerModule(DefaultScalaModule)
  }

  val pbEventType2PuppetEventName = Map[EventType,EventName.Type](
    EventType.EVENT_TYPE_DONG->        EventName.PuppetEventNameDong,
    EventType.EVENT_TYPE_ERROR->       EventName.PuppetEventNameError,
    EventType.EVENT_TYPE_HEARTBEAT->   EventName.PuppetEventNameHeartbeat,
    EventType.EVENT_TYPE_FRIENDSHIP->  EventName.PuppetEventNameFriendship,
    EventType.EVENT_TYPE_LOGIN->       EventName.PuppetEventNameLogin,
    EventType.EVENT_TYPE_LOGOUT->      EventName.PuppetEventNameLogout,
    EventType.EVENT_TYPE_MESSAGE->     EventName.PuppetEventNameMessage,
    EventType.EVENT_TYPE_READY->       EventName.PuppetEventNameReady,
    EventType.EVENT_TYPE_ROOM_INVITE-> EventName.PuppetEventNameRoomInvite,
    EventType.EVENT_TYPE_ROOM_JOIN->   EventName.PuppetEventNameRoomJoin,
    EventType.EVENT_TYPE_ROOM_LEAVE->  EventName.PuppetEventNameRoomLeave,
    EventType.EVENT_TYPE_ROOM_TOPIC->  EventName.PuppetEventNameRoomTopic,
    EventType.EVENT_TYPE_SCAN->        EventName.PuppetEventNameScan,
    EventType.EVENT_TYPE_RESET->       EventName.PuppetEventNameReset,
    EventType.EVENT_TYPE_UNSPECIFIED-> EventName.PuppetEventNameUnknown
  )

  def isBlank(value:String): Boolean = value == null || value.trim.length ==0
}
