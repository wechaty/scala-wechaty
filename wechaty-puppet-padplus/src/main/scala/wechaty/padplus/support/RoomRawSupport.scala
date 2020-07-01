package wechaty.padplus.support

import wechaty.padplus.schemas.ModelRoom.{GrpcRoomPayload, PadplusMemberBrief, PadplusRoomPayload}
import wechaty.puppet.ResourceBox
import wechaty.puppet.schemas.{Puppet, Room}
import wechaty.puppet.support.RoomSupport

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-22
  */
trait RoomRawSupport {
  self:RoomSupport =>

  override def roomAdd(roomId: String, contactId: String): Unit = ???

  override def roomAvatar(roomId: String): ResourceBox = ???

  override def roomCreate(contactIdList: Array[String], topic: String): String = ???

  override def roomDel(roomId: String, contactId: String): Unit = ???

  override def roomList(): Array[String] = ???

  override def roomQRCode(roomId: String): String = ???

  override def roomQuit(roomId: String): Unit = ???

  override def roomTopic(roomId: String): String = ???

  override def roomTopic(roomId: String, topic: String): Unit = ???

  override protected def roomRawPayload(roomId: String): Room.RoomPayload = ???

  def convertRoomFromGrpc(room: GrpcRoomPayload): PadplusRoomPayload = {
    val members=Puppet.objectMapper.readValue(room.ExtInfo,classOf[Array[PadplusMemberBrief]])
    val roomPayload = new PadplusRoomPayload
    roomPayload.alias          = room.Alias
    roomPayload.bigHeadUrl     = room.BigHeadImgUrl
    roomPayload.chatRoomOwner  = room.ChatRoomOwner
    roomPayload.chatroomId     = room.UserName
    roomPayload.chatroomVersion= room.ChatroomVersion
    roomPayload.contactType    = room.ContactType
    roomPayload.memberCount    = members.length
    roomPayload.members        = members
    roomPayload.nickName       = room.NickName
    roomPayload.smallHeadUrl   = room.SmallHeadImgUrl
    roomPayload.stranger       = room.EncryptUsername
    roomPayload.tagList        = room.LabelLists
    roomPayload.ticket         = room.Ticket

    roomPayload
  }
}
