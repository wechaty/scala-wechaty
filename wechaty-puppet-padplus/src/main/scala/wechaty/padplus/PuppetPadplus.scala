package wechaty.padplus

import com.typesafe.scalalogging.LazyLogging
import wechaty.padplus.grpc.PadPlusServerOuterClass.ApiType
import wechaty.padplus.support._
import wechaty.puppet.Puppet
import wechaty.puppet.schemas.Puppet.PuppetOptions
import wechaty.puppet.schemas._
import wechaty.puppet.support.ContactSupport

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-21
  */
class PuppetPadplus(val option:PuppetOptions,val storePath:String="/tmp/padplus")
  extends Puppet
    with ContactRawSupport
    with ContactSelfRawSupport
    with ContactSupport
    with MessageRawSupport
    with RoomRawSupport
    with PadplusHelper
    with GrpcSupport
    with GrpcEventSupport
    with LocalStoreSupport
    with LazyLogging {
  protected var uinOpt:Option[String]=None
  def start(): Unit ={
    startGrpc(option.endPoint.get)
    //waiting stream start....
    logger.info("waiting stream start....")
    awaitStreamStart()
    startLocalStore()
    getUin match{
      case Some(str) =>
        uinOpt = Some(str)
        logger.debug("found uin in local store:{}",str)
        asyncRequest(ApiType.INIT)
      case _ =>
        asyncRequest(ApiType.GET_QRCODE)
    }
  }
  def stop(): Unit = {
    stopGrpc()
    stopLocalStore()
  }

  override def selfIdOpt(): Option[String] = selfId

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

  override protected def roomMemberRawPayload(roomId: String, contactId: String): Room.RoomMemberPayload = {
    //TODO
    null
  }



  override def roomInvitationAccept(roomInvitationId: String): Unit = ???

  override protected def roomInvitationRawPayload(roomInvitationId: String): RoomInvitation.RoomInvitationPayload = ???

  override def tagContactAdd(tagId: String, contactId: String): Unit = ???

  override def tagContactDelete(tagId: String): Unit = ???

  override def tagContactList(contactId: String): Array[String] = ???

  override def tagContactList(): Array[String] = ???

  override def tagContactRemove(tagId: String, contactId: String): Unit = ???

}
