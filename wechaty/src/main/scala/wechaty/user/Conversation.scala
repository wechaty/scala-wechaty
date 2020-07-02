package wechaty.user

import wechaty.helper.ImplicitHelper._
import wechaty.Wechaty.PuppetResolver
import wechaty.puppet.ResourceBox
import wechaty.puppet.schemas.Puppet.executionContext

import scala.concurrent.Future


/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-08
  */
class Conversation(val id:String)(implicit resolver:PuppetResolver) {
  def say(something: String): Future[Message] = {
    resolver.puppet.messageSendText(this.id, something)
  }

  def say(something: Contact): Message = {
    resolver.puppet.messageSendContact(this.id, something.id)
  }

  def say(something: UrlLink): Message = {
    resolver.puppet.messageSendUrl(this.id, something.payload)
  }

  def say(something: MiniProgram): Message = {
    resolver.puppet.messageSendMiniProgram(this.id, something.payload)
  }
  def say(something: ResourceBox): Message = {
    resolver.puppet.messageSendFile(this.id, something)
  }
}
