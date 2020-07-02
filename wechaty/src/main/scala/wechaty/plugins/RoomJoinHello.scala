package wechaty.plugins

import com.typesafe.scalalogging.LazyLogging
import wechaty.{Wechaty, WechatyPlugin}

import scala.concurrent.duration._
import scala.concurrent.Await

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-23
  */
case class RoomJoinHelloConfig( var rooms:Array[String]=Array() ,hello:String="welcome")
class RoomJoinHello(config:RoomJoinHelloConfig,/*only for test*/isWait:Boolean=false) extends WechatyPlugin with LazyLogging{
  override def install(wechaty: Wechaty): Unit = {
    implicit val resolver:Wechaty = wechaty
    wechaty.onOnceMessage(message=>{
      logger.info("install RoomJoinHello Plugin....")
      val rooms = PluginHelper.findRooms(config.rooms)
      rooms.foreach(room=>{
        room.onJoin{case (list,_,_)=>
          PluginHelper.executeWithNotThrow("RoomJoinHello") {
            if (list == null || list.isEmpty) {
              logger.warn("invitee list is empty")
            } else {
              val future = room.say(config.hello, list)
              if(isWait)
                Await.result(future,10 seconds)
            }
          }
        }
      })
      logger.info("install RoomJoinHello Plugin done")
    })
  }
}
