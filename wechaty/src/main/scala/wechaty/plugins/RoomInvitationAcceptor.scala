package wechaty.plugins

import com.typesafe.scalalogging.LazyLogging
import wechaty.{Wechaty, WechatyPlugin}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-28
  */
case class RoomInvitationAcceptorConfig()
class RoomInvitationAcceptor extends WechatyPlugin with LazyLogging{
  override def install(wechaty: Wechaty): Unit = {
    logger.info("install RoomInvitationAcceptor Plugin....")
    wechaty.onRoomInvite(roomInvitation=>{
      PluginHelper.executeWithNotThrow("RoomInvitationAcceptor"){
        roomInvitation.accept()
      }
    })
    logger.info("install RoomInvitationAcceptor Plugin done.")
  }
}
