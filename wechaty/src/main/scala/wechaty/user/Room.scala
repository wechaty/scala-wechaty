package wechaty.user

import java.util.Date

import com.typesafe.scalalogging.LazyLogging
import wechaty.Wechaty.PuppetResolver
import wechaty.puppet.ResourceBox
import wechaty.puppet.events.EventEmitter
import wechaty.puppet.schemas.Event.{EventMessagePayload, EventRoomJoinPayload, EventRoomLeavePayload, EventRoomTopicPayload}
import wechaty.puppet.schemas.Puppet._
import wechaty.puppet.schemas.Room.{RoomPayload, RoomQueryFilter}
import wechaty.user.Room.{RoomJoinEvent, RoomLeaveEvent, RoomTopicEvent}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-08
  */
object Room {
  type RoomJoinEvent = (Array[Contact],Contact,Date)
  type RoomLeaveEvent=(Array[Contact],Contact,Date)
  type RoomTopicEvent=(Contact,Date)
  private var pool = Map[String,Room]()
  def create(contactList: Array[Contact], topic: String)(implicit puppetResolver: PuppetResolver): Room = {

    if (contactList.length < 2) {
      throw new Error("contactList need at least 2 contact to create a new room")
    }

    val contactIdList = contactList.map(contact => contact.id)
    val roomId = puppetResolver.puppet.roomCreate(contactIdList, topic)
    new Room(roomId)
  }
  def load(roomId:String)(implicit puppetResolver: PuppetResolver): Option[Room]={
    pool.get(roomId) match{
      case Some(room) => Some(room)
      case _ =>
//        val payload = puppetResolver.puppet.roomPayload(roomId)
//        val newRoom = new Room(payload.id)
        val newRoom = new Room(roomId)
        pool += (roomId-> newRoom)

        Some(newRoom)
    }
  }
  def messageEvent(messagePayload: EventMessagePayload)(implicit resolver: PuppetResolver):Unit = {
    val message = new Message(messagePayload.messageId)
    val roomOpt = message.room
    roomOpt.foreach(_.emit(PuppetEventName.MESSAGE,message))
  }
  def roomJoinEvent(payload:EventRoomJoinPayload)(implicit resolver: PuppetResolver): Unit ={
    val room = load(payload.roomId).get
    val inviteeList = payload.inviteeIdList.map(id => new Contact(id))
    val inviter = new Contact(payload.inviterId)
    val date = timestampToDate(payload.timestamp)
    room.emit(PuppetEventName.ROOM_JOIN,(inviteeList,inviter,date))
  }
  def roomLeaveEvent(payload:EventRoomLeavePayload)(implicit resolver: PuppetResolver): Unit ={
    val room = load(payload.roomId).get
    val leaverList = payload.removeeIdList.map(id => new Contact(id))

    val remover = new Contact(payload.removerId)
    val date = timestampToDate(payload.timestamp)
    room.emit(PuppetEventName.ROOM_LEAVE,(leaverList,remover,date))
    //invalid cache
    val userIdOpt = resolver.puppet.selfIdOpt()
    userIdOpt match{
      case Some(userId) =>
        if(leaverList.exists(_.id == userId)){
          resolver.puppet.roomPayloadDirty(payload.roomId)
          resolver.puppet.roomMemberPayloadDirty(payload.roomId)
        }
      case _ =>
    }
}
  def roomTopicEvent(payload:EventRoomTopicPayload)(implicit resolver: PuppetResolver): Unit ={
    val room = load(payload.roomId).get
    val changer = new Contact(payload.changerId)
    val date = timestampToDate(payload.timestamp)
    room.emit(PuppetEventName.ROOM_TOPIC,(changer,date))
  }
  def findAll(query : Option[RoomQueryFilter] = None)(implicit resolver: PuppetResolver): Array[Room]= {
    val roomIdList = resolver.puppet.roomSearch(query)
    roomIdList.flatMap(id => load(id))
  }
  def find(query : Option[RoomQueryFilter] = None)(implicit resolver: PuppetResolver): Option[Room] = {
    findAll(query).headOption
  }
  def find(query : RoomQueryFilter)(implicit resolver: PuppetResolver): Option[Room] = {
    find(Some(query))
  }
}

class Room private(roomId: String)(implicit resolver: PuppetResolver) extends Conversation(roomId) with EventEmitter with LazyLogging {
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
    this.say( mentionText+"\u2005 "+something )
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

  def onMessage(messageListener:Message=>Unit): Unit ={
    this.addListener(PuppetEventName.MESSAGE,messageListener)
  }
  def onJoin(joinListener:RoomJoinEvent =>Unit): Unit ={
    this.addListener(PuppetEventName.ROOM_JOIN,joinListener)
  }
  def onLeave(leaveListener:RoomLeaveEvent =>Unit): Unit ={
    this.addListener(PuppetEventName.ROOM_LEAVE,leaveListener)
  }
  def onTopic(topicListener:RoomTopicEvent =>Unit): Unit ={
    this.addListener(PuppetEventName.ROOM_TOPIC,topicListener)
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
