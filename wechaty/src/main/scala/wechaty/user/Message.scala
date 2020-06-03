package wechaty.user

import wechaty.puppet.Puppet
import wechaty.puppet.schemas
import wechaty.puppet.schemas.Puppet

/**
  * wrap MessagePayload
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
class Message(messageId:String)(implicit puppet:Puppet) {
  lazy val payload: schemas.Message.MessagePayload = puppet.messagePayload(messageId)
  def say(text:String): Unit = {
    puppet.messageSendText(sayId(),text)
  }
  private def sayId(): String ={
    if(!Puppet.isBlank(payload.roomId)) payload.roomId
    else if(!Puppet.isBlank(payload.fromId)) payload.fromId
    else throw new IllegalStateException("roomid and fromid both is null")
  }

  override def toString: String = {
    payload.text
  }
}
