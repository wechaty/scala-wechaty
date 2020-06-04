package wechaty.user

import wechaty.Wechaty.PuppetResolver
import wechaty.puppet.schemas
import wechaty.puppet.schemas.Puppet

/**
  * wrap MessagePayload
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
class Message(messageId:String)(implicit resolver: PuppetResolver) {
  lazy val payload: schemas.Message.MessagePayload = resolver.puppet.messagePayload(messageId)
  def say(text:String): Unit = {
    resolver.puppet.messageSendText(sayId(),text)
  }
  private def sayId(): String ={
    if(!Puppet.isBlank(payload.roomId)) payload.roomId
    else if(!Puppet.isBlank(payload.fromId)) payload.fromId
    else throw new IllegalStateException("roomid and fromid both is null")
  }

  override def toString: String = {
    if(payload!= null ) payload.text else ""
  }
}
