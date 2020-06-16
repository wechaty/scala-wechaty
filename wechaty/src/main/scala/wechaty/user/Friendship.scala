package wechaty.user

import wechaty.Wechaty.PuppetResolver
import wechaty.puppet.schemas.Friendship.{FriendshipPayload, FriendshipPayloadReceive, FriendshipType}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-15
  */
object Friendship{
  def add ( contact : Contact, hello   : String)(implicit puppetResolver: PuppetResolver): Unit = {
    puppetResolver.puppet.friendshipAdd(contact.id, hello)
  }
}
class Friendship(val id:String)(implicit puppetResolver: PuppetResolver) {
  private def puppet= puppetResolver.puppet
  def payload :FriendshipPayload = {
    puppetResolver.puppet.friendshipPayload(id)
  }
  private def assertPayload(): Unit ={
    if (this.payload == null) {
      throw new Error("no payload")
    }
  }
  def contact(): Contact ={
    assertPayload()
    new Contact(this.payload.contactId)
  }
  def accept (): Unit =  {
    assertPayload()
    if (!this.payload.isInstanceOf[FriendshipPayloadReceive]) {
    throw new Error("accept() need type to be FriendshipType.Receive, but it got a " + this.payload.`type`)
  }

    puppet.friendshipAccept(this.id)

    val contact = this.contact()
    contact.sync()
  }

  def hello (): String = {
    assertPayload()
    payload.hello
  }
  def `type`: FriendshipType.Type ={
    assertPayload()
    payload.`type`
  }
}
