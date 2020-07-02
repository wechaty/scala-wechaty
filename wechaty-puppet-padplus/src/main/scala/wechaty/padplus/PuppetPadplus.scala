package wechaty.padplus

import com.typesafe.scalalogging.LazyLogging
import wechaty.padplus.grpc.PadPlusServerOuterClass.{ApiType, ResponseType, StreamResponse}
import wechaty.padplus.support._
import wechaty.puppet.Puppet
import wechaty.puppet.schemas.Event.EventResetPayload
import wechaty.puppet.schemas.Puppet.{PuppetEventName, PuppetOptions}
import wechaty.puppet.schemas._
import wechaty.puppet.support.ContactSupport

import scala.concurrent.Future

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
    with DisrutporSupport
    with LazyLogging {
  protected var uinOpt:Option[String]=None
  private[wechaty] def startGrpc(): Unit ={
    startLocalStore()
    startDisruptor()
    startGrpc(option.endPoint.get)
  }
  def start(): Unit ={
    startGrpc()
    startAwaitStream()
  }
  private[wechaty] def startAwaitStream(): Unit ={
    startStream()
    //waiting stream start....
    logger.info("waiting stream start....")
    awaitStreamStart()
    getUin match{
      case Some(str) =>
        uinOpt = Some(str)
        logger.debug("found uin in local store:{}",str)
        asyncRequestNothing(ApiType.INIT)
      case _ =>
        asyncRequestNothing(ApiType.GET_QRCODE)
    }
  }
  def stop(): Unit = {
    shutdownDisruptor()
    stopGrpc()
    stopLocalStore()
  }
  protected def sysPartialFunction(response: StreamResponse): PartialFunction[ResponseType, Unit] = {
    case ResponseType.DISCONNECT =>
      emit(PuppetEventName.RESET,new EventResetPayload)
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

  override protected def roomMemberRawPayload(roomId: String, contactId: String): Future[Room.RoomMemberPayload] = {
    ???
  }



  override def roomInvitationAccept(roomInvitationId: String): Unit = ???

  override protected def roomInvitationRawPayload(roomInvitationId: String): RoomInvitation.RoomInvitationPayload = ???

  override def tagContactAdd(tagId: String, contactId: String): Unit = ???

  override def tagContactDelete(tagId: String): Unit = ???

  override def tagContactList(contactId: String): Array[String] = ???

  override def tagContactList(): Array[String] = ???

  override def tagContactRemove(tagId: String, contactId: String): Unit = ???

}
