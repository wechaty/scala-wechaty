package wechaty.user

import wechaty.Wechaty.PuppetResolver
import wechaty.helper.ImplicitHelper._
import wechaty.puppet.schemas.Message.MessageType
import wechaty.puppet.schemas.Puppet
import wechaty.puppet.schemas.Puppet._
import wechaty.puppet.{ResourceBox, schemas}


/**
  * wrap MessagePayload
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
class Message(messageId:String)(implicit resolver: PuppetResolver) {
  private val MENTION_MEMBER_PATTERN= ("@([^\u2005^\u0020^$]+)")
  lazy val payload: schemas.Message.MessagePayload = {
    resolver
      .puppet
      .messagePayload(messageId)
  }
  private def sayId: String ={
    if(!Puppet.isBlank(payload.roomId)) payload.roomId
    else if(!Puppet.isBlank(payload.fromId)) payload.fromId
    else throw new IllegalStateException("roomid and fromid both is null")
  }

  override def toString: String = {
    if(payload != null) payload.text
    else "Message"
  }
  def talker:Contact ={
    this.from
  }

  def conversation : Conversation = {
    if (this.room != null) this.room
    else this.from
  }
  private def assertPayload(): Unit ={
    if (this.payload == null) {
      throw new Error("no payload")
    }
  }

  def from: Contact ={
    assertPayload()
    val fromId = this.payload.fromId
    if (Puppet.isBlank(fromId)) null
    else new Contact(fromId)
  }

  def to : Contact ={
    assertPayload()

    val toId = this.payload.toId
    if (Puppet.isBlank(toId)) null
    else new Contact(toId)
  }

  def room :Room ={
    assertPayload()
    val roomId = this.payload.roomId
    if (isBlank(roomId)) null
    else new Room(roomId)
  }


  def text : String ={
    assertPayload()
    this.payload.text
  }

  def toRecalled: Message= {
    if (this.`type` != MessageType.Recalled) {
      throw new Error("Can not call toRecalled() on message which is not recalled type.")
    }
    val originalMessageId = this.text
    if (originalMessageId == null) {
      throw new Error("Can not find recalled message")
    }
    new Message(originalMessageId)
  }

  def say(text:String): Message = {
    resolver.puppet.messageSendText(sayId,text)
  }
  def say(contact: Contact): Message = {
    resolver.puppet.messageSendContact(sayId,contact.id)
  }
  def say(resourceBox:ResourceBox): Message ={
    resolver.puppet.messageSendFile(sayId,resourceBox)
  }
  def say(urlLink: UrlLink) :Message = {
    resolver.puppet.messageSendUrl(sayId,urlLink.payload)
  }
  def say(mp:MiniProgram) :Message = {
    resolver.puppet.messageSendMiniProgram(
      sayId,
      mp.payload,
    )
  }

  def recall (): Boolean = {
    resolver.puppet.messageRecall(messageId)
  }
  def `type`: MessageType.Type={
    assertPayload()
    this.payload.`type`
  }

  def self (): Boolean = {
    val userIdOpt = resolver.puppet.selfIdOpt()
    userIdOpt match{
      case Some(userId) =>
        val from = this.from
        from != null && from.id == userId
      case _ =>
        false
    }
  }

  def mentionList: Array[Contact]= {
    val room = this.room
    if (this.`type` != MessageType.Text || room == null) {
      return Array()
    }

    /**
      * Use mention list if mention list is available
      * otherwise, process the message and get the mention list
      */
    if (this.payload != null && this.payload.mentionIdList != null && this.payload.mentionIdList.length > 0) {
      this.payload.mentionIdList.map(new Contact(_))
    } else {
      val reg = MENTION_MEMBER_PATTERN.r
      val it = reg.findAllMatchIn(text)
      it.map(_.group(1)).map(new Contact(_)).toArray

    }
  }

  def mentionText (): String= {
    val text = this.text
    val room = this.room

    val list = this.mentionList

    if (room == null || list == null || list.length == 0) {
      return text
    }

    val toAliasName = (member: Contact) => {
      val alias = room.alias(member)
      val name = member.name
      if(isBlank(alias)) name else alias
    }

    val mentionNameList = list.map(toAliasName)

    val textWithoutMention = mentionNameList.foldLeft(text)((prev, cur) => {
      val escapedCur = cur
      val regex = "@\\Q"+escapedCur+"\\E(\\u2005|\\u0020|$)"
      prev.replaceFirst(regex,"")
    })

    textWithoutMention.trim()
  }
}
