package wechaty.user

import wechaty.hostie.PuppetHostie
import wechaty.puppet.schemas.Message.MessagePayload

/**
  * wrap MessagePayload
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
class Message(val payload:MessagePayload,puppet:PuppetHostie) {
  def say(text:String): Unit = {
    puppet.messageSendText(sayId(),text)
  }
  def sayId(): String ={
    if(payload.roomId != null)
      payload.roomId
    else if(payload.fromId != null) {
      payload.fromId
    }else {
      throw new IllegalStateException("roomid and fromid both is null")
    }
  }

  override def toString: String = {
    payload.text
  }
}
