package wechaty.plugins

import java.util.concurrent.TimeUnit

import com.typesafe.scalalogging.LazyLogging
import wechaty.Wechaty.PuppetResolver
import wechaty.user.{Message, Room}
import wechaty.{Wechaty, WechatyPlugin}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-19
  */
case class RoomConnectorConfig(
                                var from: Array[String],
                                var to: Array[String],
                                var mapper: Message => Option[Message] = msg => Some(msg),
                                var blacklist: Message => Boolean = _ => false,
                                var whitelist: Message => Boolean = _ => true
                              )

class RoomConnector(config: RoomConnectorConfig) extends WechatyPlugin with LazyLogging {
  override def install(wechaty: Wechaty): Unit = {
    implicit val resolver = wechaty
    wechaty.onOnceMessage(message => {
      val fromRooms           = findRooms(config.from)
      val toRooms             = findRooms(config.to)
      val roomMessageListener = (roomMessage: Message) => {
        if (config.whitelist(roomMessage) && !config.blacklist(roomMessage)) {
          config.mapper(roomMessage) match {
            case Some(msg) =>
              toRooms.foreach(msg.forward)
            case _ => //filtered,so don't forward message
          }
        }
      }
      //process current message
      message.room match {
        case Some(r) =>
          if (fromRooms.exists(_.id == r.id)) {
            roomMessageListener(message)
          }
        case _ =>
      }

      fromRooms.foreach(room => {
        room.onMessage(roomMessage => {
          roomMessageListener(roomMessage)
        })
      })
    })
  }

  private def findRooms(roomIds: Array[String])(implicit resolver: PuppetResolver): Array[Room] = {
    try {
      roomIds.flatMap(Room.load(_)) //avoid timeout ?
    } catch {
      case e: Throwable =>
        logger.warn("load room occurs exception,so loop load", e)
        Thread.sleep(TimeUnit.SECONDS.toMillis(5)) //sleep 5s to wait
        findRooms(roomIds)
    }
  }
}
