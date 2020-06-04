package wechaty.puppet.schemas

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.github.wechaty.grpc.puppet.Event.EventType
import wechaty.puppet.schemas.Events.PuppetEventName

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

  val pbEventType2PuppetEventName = Map[EventType,PuppetEventName.Type](
    EventType.EVENT_TYPE_DONG->        PuppetEventName.DONG,
    EventType.EVENT_TYPE_ERROR->       PuppetEventName.ERROR,
    EventType.EVENT_TYPE_HEARTBEAT->   PuppetEventName.HEARTBEAT,
    EventType.EVENT_TYPE_FRIENDSHIP->  PuppetEventName.FRIENDSHIP,
    EventType.EVENT_TYPE_LOGIN->       PuppetEventName.LOGIN,
    EventType.EVENT_TYPE_LOGOUT->      PuppetEventName.LOGOUT,
    EventType.EVENT_TYPE_MESSAGE->     PuppetEventName.MESSAGE,
    EventType.EVENT_TYPE_READY->       PuppetEventName.READY,
    EventType.EVENT_TYPE_ROOM_INVITE-> PuppetEventName.INVITE,
    EventType.EVENT_TYPE_ROOM_JOIN->   PuppetEventName.ROOM_JOIN,
    EventType.EVENT_TYPE_ROOM_LEAVE->  PuppetEventName.ROOM_LEAVE,
    EventType.EVENT_TYPE_ROOM_TOPIC->  PuppetEventName.ROOM_TOPIC,
    EventType.EVENT_TYPE_SCAN->        PuppetEventName.SCAN,
    EventType.EVENT_TYPE_RESET->       PuppetEventName.RESET,
    EventType.EVENT_TYPE_UNSPECIFIED-> PuppetEventName.UNKNOWN
  )

  def isBlank(value:String): Boolean = value == null || value.trim.length ==0
}
