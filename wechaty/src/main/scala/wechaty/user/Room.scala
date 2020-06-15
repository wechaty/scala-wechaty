package wechaty.user

import wechaty.Wechaty.PuppetResolver
import wechaty.puppet.schemas.Puppet.isBlank
import wechaty.puppet.schemas.Room.RoomPayload
import wechaty.puppet.{LoggerSupport, ResourceBox}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-08
  */
object Room {
  def create(contactList: Array[Contact], topic: String)(implicit puppetResolver: PuppetResolver): Room = {

    if (contactList.length < 2) {
      throw new Error("contactList need at least 2 contact to create a new room")
    }

    val contactIdList = contactList.map(contact => contact.id)
    val roomId = puppetResolver.puppet.roomCreate(contactIdList, topic)
    new Room(roomId)
  }
}

class Room(roomId: String)(implicit resolver: PuppetResolver) extends Conversation(roomId) with LoggerSupport {
  def payload: RoomPayload = {
    resolver.puppet.roomPayload(roomId)
  }

  def alias(contact: Contact): Option[String] = {
    val memberPayload = resolver.puppet.roomMemberPayload(this.id, contact.id)

    if (memberPayload != null && !isBlank(memberPayload.roomAlias)) {
      Some(memberPayload.roomAlias)
    } else None
  }

  def memberList(): Array[Contact] = {
    val memberIdList = resolver.puppet.roomMemberList(this.roomId)
    memberIdList.map(new Contact(_))
  }

  def sync(): Unit = {
    resolver.puppet.roomPayloadDirty(this.roomId)
  }

  def say(something: String, mentionList: Array[Contact]): Message = {
    val mentionText = mentionList.map(x => {
      val aliasOpt = this.alias(x)
      aliasOpt match {
        case Some(alias) => '@' + alias
        case _ => '@' + x.name
      }
    }).mkString("\u2005")
    this.say(something + mentionText)
  }

  def add(contact: Contact): Unit = {
    resolver.puppet.roomAdd(this.id, contact.id)
  }

  def del(contact: Contact): Unit = {
    resolver.puppet.roomDel(this.id, contact.id)
  }

  def quit(): Unit = {
    resolver.puppet.roomQuit(this.id)
  }

  def topic(newTopicOpt: Option[String] = None): String = {
    newTopicOpt match {
      case Some(newTopic) =>
        resolver.puppet.roomTopic(this.id, newTopic)
        newTopic
      case _ =>
        if (this.payload != null && !isBlank(this.payload.topic)) {
          this.payload.topic
        } else {
          val memberIdList = resolver.puppet.roomMemberList(this.id)
          val memberList = memberIdList
            .filter(id => resolver.puppet.selfIdOpt() match {
              case Some(userId) => id != userId
              case _ => true
            })
            .map(id => new Contact(id))

          memberList.take(3).map(_.name).mkString(",")
        }
    }
  }

  def announce(textOpt: Option[String]): String = {
    textOpt match {
      case Some(text) =>
        resolver.puppet.roomAnnounce(this.id, text)
        text
      case _ =>
        resolver.puppet.roomAnnounce(this.id)
    }
  }

  def qrCode(): String = {
    resolver.puppet.roomQRCode(this.id)
  }

  def has(contact: Contact): Boolean = {
    this.memberList().exists(_.id == contact.id)
  }

  def owner(): Option[Contact] = {
    if (this.payload != null && !isBlank(this.payload.ownerId)) {
      Some(new Contact(this.payload.ownerId))
    } else None
  }

  def avatar(): ResourceBox = {
    resolver.puppet.roomAvatar(this.id)
  }
}
