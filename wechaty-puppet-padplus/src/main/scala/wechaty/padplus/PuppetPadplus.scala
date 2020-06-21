package wechaty.padplus

import com.typesafe.scalalogging.LazyLogging
import wechaty.padplus.grpc.PadPlusServerOuterClass.ApiType
import wechaty.padplus.support.{ContactRawSupport, GrpcEventSupport, GrpcSupport}
import wechaty.puppet.schemas.Image.ImageType.Type
import wechaty.puppet.schemas.Puppet.PuppetOptions
import wechaty.puppet.schemas._
import wechaty.puppet.support.ContactSupport
import wechaty.puppet.{Puppet, ResourceBox}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-21
  */
class PuppetPadplus(val option:PuppetOptions)
  extends Puppet
    with ContactRawSupport
    with ContactSupport
    with GrpcSupport
    with GrpcEventSupport
    with LazyLogging {
  def start(): Unit ={
    startGrpc(option.endPoint.get)
    request(ApiType.GET_QRCODE)
  }
  def stop(): Unit = {
    stopGrpc()
  }

  override def selfIdOpt(): Option[String] = None

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

  /**
    *
    * Friendship
    *
    */
  override def friendshipAccept(friendshipId: String): Unit = ???

  override def friendshipAdd(contactId: String, hello: String): Unit = ???

  override def friendshipSearchPhone(phone: String): String = ???

  override def friendshipSearchWeixin(weixin: String): String = ???

  override protected def friendshipRawPayload(friendshipId: String): Friendship.FriendshipPayload = ???

  /**
    *
    * RoomMember
    *
    */
  override def roomAnnounce(roomId: String): String = ???

  override def roomAnnounce(roomId: String, text: String): Unit = ???

  override def roomMemberList(roomId: String): Array[String] = ???

  override protected def roomMemberRawPayload(roomId: String, contactId: String): Room.RoomMemberPayload = ???

  /**
    * message
    */
  override def messageContact(messageId: String): String = ???

  override def messageFile(messageId: String): ResourceBox = ???

  override def messageImage(messageId: String, imageType: Type): ResourceBox = ???

  override def messageMiniProgram(messageId: String): MiniProgram.MiniProgramPayload = ???

  override def messageUrl(messageId: String): UrlLink.UrlLinkPayload = ???

  override def messageSendContact(conversationId: String, contactId: String): String = ???

  override def messageSendFile(conversationId: String, file: ResourceBox): String = ???

  override def messageSendMiniProgram(conversationId: String, miniProgramPayload: MiniProgram.MiniProgramPayload): String = ???

  override def messageSendText(conversationId: String, text: String, mentionIdList: Array[String]): String = ???

  override def messageSendUrl(conversationId: String, urlLinkPayload: UrlLink.UrlLinkPayload): String = ???

  override def messageRecall(messageId: String): Boolean = ???

  override def messageSendText(conversationID: String, text: String, mentionIDList: String*): String = ???

  override protected def messageRawPayload(messageId: String): Message.MessagePayload = ???

  override protected def ding(data: String): Unit = ???

  override def roomInvitationAccept(roomInvitationId: String): Unit = ???

  override protected def roomInvitationRawPayload(roomInvitationId: String): RoomInvitation.RoomInvitationPayload = ???

  override def tagContactAdd(tagId: String, contactId: String): Unit = ???

  override def tagContactDelete(tagId: String): Unit = ???

  override def tagContactList(contactId: String): Array[String] = ???

  override def tagContactList(): Array[String] = ???

  override def tagContactRemove(tagId: String, contactId: String): Unit = ???

  /**
    *
    * ContactSelf
    *
    */
  override def contactSelfName(name: String): Unit = ???

  override def contactSelfQRCode(): String = ???

  override def contactSelfSignature(signature: String): Unit = ???

  override def logout(): Unit = ???

}
