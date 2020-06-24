package wechaty.user

import java.util.Date

import com.typesafe.scalalogging.LazyLogging
import wechaty.Wechaty.PuppetResolver
import wechaty.helper.ImplicitHelper._
import wechaty.puppet.schemas.Message.MessageType
import wechaty.puppet.schemas.Puppet._
import wechaty.puppet.schemas.Puppet
import wechaty.puppet.{ResourceBox, schemas}


/**
  * wrap MessagePayload
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
class Message(messageId:String)(implicit resolver: PuppetResolver) extends LazyLogging{
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
    room match{
      case Some(r) => r
      case _ => this.from
    }
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

  def room:Option[Room] ={
    assertPayload()
    val roomId = this.payload.roomId
    if (isBlank(roomId)) None
    else Room.load(roomId)
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

    if (room.isEmpty || list == null || list.length == 0) {
      return text
    }

    val toAliasName = (member: Contact) => {
      room.get.alias(member).getOrElse(member.name)
    }

    logger.debug(s"message text:$text mentionsList:$list")
    val mentionNameList = list.map(toAliasName)

    val textWithoutMention = mentionNameList.foldLeft(text)((prev, cur) => {
      val escapedCur = cur
      val regex = "@\\Q"+escapedCur+"\\E(\\u2005|\\u0020|$)"
      prev.replaceFirst(regex,"")
    })

    textWithoutMention.trim()
  }
  def mentionSelf (): Boolean =  {
    resolver.puppet.selfIdOpt() match{
      case Some(selfId) =>
        mentionList.exists(_.id == selfId)
      case _ =>  false
    }
  }
  def forward (to: Conversation): Unit = {
    resolver.puppet.messageForward(to.id, this.messageId)
  }
  /**
    * Message sent date
    */
  def date: Date ={
    assertPayload()
    val timestamp = this.payload.timestamp
    timestampToDate(timestamp)
  }

  /**
    * Returns the message age in seconds. <br>
    *
    * For example, the message is sent at time `8:43:01`,
    * and when we received it in Wechaty, the time is `8:43:15`,
    * then the age() will return `8:43:15 - 8:43:01 = 14 (seconds)`
    * @returns {number}
    */
  def age:Long={
    val ageMilliseconds = System.currentTimeMillis() - this.date.getTime()
    val ageSeconds = Math.floor(ageMilliseconds / 1000)
    ageSeconds.longValue()
  }


  /**
    * Extract the Media File from the Message, and put it into the FileBox.
    * > Tips:
    * This function is depending on the Puppet Implementation, see [puppet-compatible-table](https://github.com/wechaty/wechaty/wiki/Puppet#3-puppet-compatible-table)
    *
    * @returns {Promise<FileBox>}
    *
    * @example <caption>Save media file from a message</caption>
    * const fileBox = await message.toFileBox()
    * const fileName = fileBox.name
    * fileBox.toFile(fileName)
    */
  def toResourceBox (): ResourceBox= {
    if (this.`type` == MessageType.Text) {
    throw new Error("text message no file")
    }
    resolver.puppet.messageFile(this.messageId)
  }
  def toImage (): Image ={
    if (this.`type` != MessageType.Image) {
      throw new Error("not a image type message. type: "+this.`type`)
    }
    new Image(this.messageId)
  }

  /**
    * Get Share Card of the Message
    * Extract the Contact Card from the Message, and encapsulate it into Contact class
    * > Tips:
    * This function is depending on the Puppet Implementation, see [puppet-compatible-table](https://github.com/wechaty/wechaty/wiki/Puppet#3-puppet-compatible-table)
    * @returns {Promise<Contact>}
    */
  def toContact (): Contact ={
    if (this.`type` != MessageType.Contact) {
      throw new Error("message not a ShareCard")
    }

    val contactId = resolver.puppet.messageContact(this.messageId)

    if (Puppet.isBlank(contactId)) {
    throw new Error(s"can not get Contact id by message: ${contactId}")
    }

    new Contact(contactId)
  }

  def toUrlLink (): UrlLink= {
    assertPayload()
    if (this.`type` != MessageType.Url) {
      throw new Error("message not a Url Link")
    }

    val urlPayload = resolver.puppet.messageUrl(this.messageId)
    if (urlPayload == null) {
    throw new Error(s"no url payload for message ${this.messageId}")
    }

    new UrlLink(urlPayload)
  }

  def toMiniProgram (): MiniProgram= {
    assertPayload()

    if (this.`type` != MessageType.MiniProgram) {
      throw new Error("message not a MiniProgram")
    }

    val miniProgramPayload = resolver.puppet.messageMiniProgram(this.messageId)

    if (miniProgramPayload == null) {
      throw new Error(s"no miniProgram payload for message ${this.messageId}")
    }

    new MiniProgram(miniProgramPayload)
  }
}
