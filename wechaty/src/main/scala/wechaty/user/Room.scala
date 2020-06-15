package wechaty.user

import wechaty.Wechaty.PuppetResolver
import wechaty.puppet.LoggerSupport
import wechaty.puppet.schemas.Puppet

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-08
  */
object Room{
  def create (contactList: Array[Contact], topic: String)(implicit puppetResolver: PuppetResolver): Room = {

    if (contactList.length < 2) {
      throw new Error("contactList need at least 2 contact to create a new room")
    }

    val contactIdList = contactList.map(contact => contact.id)
    val roomId = puppetResolver.puppet.roomCreate(contactIdList, topic)
    new Room(roomId)
  }
}
class Room(roomId:String)(implicit resolver:PuppetResolver) extends Conversation(roomId) with  LoggerSupport {
  def payload = {
    resolver.puppet.roomPayload()
  }

  def alias(member: Contact): String = {
    //TODO
    member.name
    //    throw new UnsupportedOperationException
  }

  def memberList(): Array[Contact] = {
    val memberIdList = resolver.puppet.roomMemberList(this.roomId)
    memberIdList.map(new Contact(_))
  }

  def sync(): Unit = {
    resolver.puppet.roomPayloadDirty(this.roomId)
  }
  def say(something: String,mentionList:Array[Contact]): Message = {
    val mentionText = mentionList.map(x=>{
      val alias = this.alias(x)
      if(Puppet.isBlank(alias)) '@'+x.name
      else '@'+alias
    }).mkString("\u2005")
    this.say(something+mentionText)
  }
}
