package wechaty.puppet.schemas

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.github.wechaty.grpc.puppet.Event.EventType

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
