package wechaty.plugins

import wechaty.user.Message
import wechaty.{Wechaty, WechatyPlugin}

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-19
  */
case class DingDongConfig(
  /**
    * Whether response to the self message
    */
  var self :Boolean = true,
  /**
    * Whether response the Room Message with mention self.
    * Default: true
    */
  var at :Boolean = true,
  /**
    * Whether response to the Direct Message
    * Default: true
    */
  var dm :Boolean = true,
  /**
    * Whether response in the Room
    * Default: true
    */
  var room :Boolean= true,

  /**
    * ding regexp expression
    */
  var dingReg:String="^#ding$",
)
class DingDongPlugin(config:DingDongConfig,/*only for test*/isWait:Boolean=false) extends WechatyPlugin{
  private val DONG="dong"
  private val DING_REGEXP=("("+config.dingReg+")").r
  private def isMatch(message: Message): Boolean ={
    if (!config.self) {
      if (message.self()) {
        return false
      }
    }

    if (config.room) {
      if (message.room.isDefined) {
        return true
      }
    }

    if (config.dm) {
      if (message.room.isEmpty) {
        return true
      }
    }

    if (config.at) {
      if (message.room.isDefined && message.mentionSelf()) {
        return true
      }
    }

    false
  }
  override def install(wechaty: Wechaty): Unit = {
    wechaty.onMessage(message=>{
      PluginHelper.executeWithNotThrow("DingDong") {
        val text = message.room match {
          case Some(_) => message.mentionText()
          case _ => message.text
        }

        text match {
          case DING_REGEXP(_) if isMatch(message) =>
            val future = message.say(DONG)
            if(isWait)
              Await.ready(future,5 seconds)
          case _ =>
        }
      }
    })
  }
}
