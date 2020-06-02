package wechaty.hostie

import io.github.wechaty.grpc.puppet.Event.{EventResponse, EventType}
import io.grpc.stub.StreamObserver
import wechaty.puppet.LoggerSupport
import wechaty.puppet.events.EventEmitter
import wechaty.puppet.schemas.Contact.ContactPayload
import wechaty.puppet.schemas.Events._
import wechaty.puppet.schemas.Message.MessagePayload
import wechaty.puppet.schemas.Puppet

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
trait GrpcEventSupport extends StreamObserver[EventResponse] {
  self: LoggerSupport with ContactSupport with MessageSupport =>
  protected var idOpt: Option[String] = None

  override def onNext(v: EventResponse): Unit = {
    if (v.getType != EventType.EVENT_TYPE_HEARTBEAT) {
      val hearbeat = new EventHeartbeatPayload
      hearbeat.data = "onGrpcStreamEvent(%s)".format(v.getType)
      EventEmitter.emit(EventName.PuppetEventNameHeartbeat, hearbeat)
    }
    v.getType match {
      case EventType.EVENT_TYPE_UNSPECIFIED =>
        error("PuppetHostie onGrpcStreamEvent() got an EventType.EVENT_TYPE_UNSPECIFIED ")
      case other =>
        val eventName = Puppet.pbEventType2PuppetEventName.getOrElse(other, throw new IllegalAccessException("unsupport event " + other))
        val payload = unMarshal(other, v.getPayload)

        other match {
          case EventType.EVENT_TYPE_RESET =>
            warn("PuppetHostie onGrpcStreamEvent() got an EventType.EVENT_TYPE_RESET ?")
            EventEmitter.emit(eventName, payload)
          case EventType.EVENT_TYPE_LOGIN =>
            val loginPayload=payload.asInstanceOf[EventLoginPayload]
            idOpt = Some(loginPayload.contactId)
            EventEmitter.emit[EventLoginPayload,ContactPayload](eventName, loginPayload)(toContactPayload)
          case EventType.EVENT_TYPE_MESSAGE =>
            val messagePayload=payload.asInstanceOf[EventMessagePayload]
            EventEmitter.emit[EventMessagePayload,MessagePayload](eventName, messagePayload)(toMessagePayload)
          case EventType.EVENT_TYPE_LOGOUT =>
            idOpt = None
            EventEmitter.emit(eventName, payload)
          case _ =>
            EventEmitter.emit(eventName, payload)
        }
    }
  }

  def unMarshal(eventType: EventType, data: String): EventPayload = {
    eventType match {
      case EventType.EVENT_TYPE_SCAN =>
        Puppet.objectMapper.readValue(data, classOf[EventScanPayload])
      case EventType.EVENT_TYPE_DONG =>
        Puppet.objectMapper.readValue(data, classOf[EventDongPayload])
      case EventType.EVENT_TYPE_ERROR =>
        Puppet.objectMapper.readValue(data, classOf[EventErrorPayload])
      case EventType.EVENT_TYPE_HEARTBEAT =>
        Puppet.objectMapper.readValue(data, classOf[EventHeartbeatPayload])
      case EventType.EVENT_TYPE_FRIENDSHIP =>
        Puppet.objectMapper.readValue(data, classOf[EventFriendshipPayload])
      case EventType.EVENT_TYPE_LOGIN =>
        Puppet.objectMapper.readValue(data, classOf[EventLoginPayload])
      case EventType.EVENT_TYPE_LOGOUT =>
        Puppet.objectMapper.readValue(data, classOf[EventLogoutPayload])
      case EventType.EVENT_TYPE_MESSAGE =>
        Puppet.objectMapper.readValue(data, classOf[EventMessagePayload])
      case EventType.EVENT_TYPE_READY =>
        Puppet.objectMapper.readValue(data, classOf[EventReadyPayload])
      case EventType.EVENT_TYPE_ROOM_INVITE =>
        Puppet.objectMapper.readValue(data, classOf[EventRoomInvitePayload])
      case EventType.EVENT_TYPE_ROOM_JOIN =>
        Puppet.objectMapper.readValue(data, classOf[EventRoomJoinPayload])
      case EventType.EVENT_TYPE_ROOM_LEAVE =>
        Puppet.objectMapper.readValue(data, classOf[EventRoomLeavePayload])
      case EventType.EVENT_TYPE_ROOM_TOPIC =>
        Puppet.objectMapper.readValue(data, classOf[EventRoomTopicPayload])
      case EventType.EVENT_TYPE_RESET =>
        Puppet.objectMapper.readValue(data, classOf[EventResetPayload])
      case other =>
        throw new IllegalAccessException("event not supported ,event:" + other)
    }
  }

  override def onError(throwable: Throwable): Unit = {
    error("Grpc onError",throwable)
  }

  override def onCompleted(): Unit = {
    info("completed")
  }
}
