package wechaty.hostie.support

import io.github.wechaty.grpc.puppet.Friendship
import wechaty.puppet.Puppet
import wechaty.puppet.schemas.Friendship._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-06
  */
trait FriendshipRawSupport {
  self: Puppet with GrpcSupport =>
  /**
    *
    * Friendship
    *
    */
  override def friendshipAccept(friendshipId: String): Unit = {
    val request = Friendship.FriendshipAcceptRequest.newBuilder()
      .setId(friendshipId)
      .build()
    grpcClient.friendshipAccept(request)
  }

  override def friendshipAdd(contactId: String, hello: String): Unit = {
    val request = Friendship.FriendshipAddRequest.newBuilder()
      .setContactId(contactId)
      .setHello(hello)
      .build()

    grpcClient.friendshipAdd(request)
  }

  override def friendshipSearchPhone(phone: String): String = {
    val request = Friendship.FriendshipSearchPhoneRequest.newBuilder()
      .setPhone(phone)
      .build()
    val response = grpcClient.friendshipSearchPhone(request)
    response.getContactId.getValue
  }

  override def friendshipSearchWeixin(weixin: String): String = {
    val request = Friendship.FriendshipSearchWeixinRequest.newBuilder()
      .setWeixin(weixin)
      .build()

    val response = grpcClient.friendshipSearchWeixin(request)
    response.getContactId.getValue
  }

  override protected def friendshipRawPayload(friendshipId: String): wechaty.puppet.schemas.Friendship.FriendshipPayload = {
    val request = Friendship.FriendshipPayloadRequest.newBuilder()
      .setId(friendshipId)
      .build()

    val response = grpcClient.friendshipPayload(request)
    val payload =
    response.getType match {
      case Friendship.FriendshipType.FRIENDSHIP_TYPE_CONFIRM =>
        val confirm = new FriendshipPayloadConfirm()
        confirm
      case Friendship.FriendshipType.FRIENDSHIP_TYPE_RECEIVE=>
        val receive = new FriendshipPayloadReceive()
        receive.scene = FriendshipSceneType.apply(response.getSceneValue)
        receive.stranger = response.getStranger
        receive.ticket = response.getTicket
        receive
      case Friendship.FriendshipType.FRIENDSHIP_TYPE_VERIFY=>
        new FriendshipPayloadVerify()
      case other =>
        new FriendshipPayload()
    }

    payload.hello = response.getHello
    payload.`type` = FriendshipType.apply(response.getTypeValue)
    payload.contactId = response.getContactId
    payload.id = response.getId

    payload
  }
}
