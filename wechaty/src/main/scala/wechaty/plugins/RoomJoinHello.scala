package wechaty.plugins

import java.util.concurrent.TimeUnit

import com.typesafe.scalalogging.LazyLogging
import wechaty.Wechaty.PuppetResolver
import wechaty.user.Room
import wechaty.{Wechaty, WechatyPlugin}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-23
  */
case class RoomJoinHelloConfig( var rooms:Array[String]=Array() ,hello:String="welcome")
class RoomJoinHello(config:RoomJoinHelloConfig) extends WechatyPlugin with LazyLogging{
  override def install(wechaty: Wechaty): Unit = {
    implicit val resolver:Wechaty = wechaty
    wechaty.onOnceMessage(message=>{
      val rooms = PluginHelper.findRooms(config.rooms)
      rooms.foreach(room=>{
        room.onJoin{case (list,_,_)=>
          room.say(config.hello,list)
        }
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
