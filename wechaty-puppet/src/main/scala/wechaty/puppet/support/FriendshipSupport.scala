package wechaty.puppet.support

import wechaty.puppet.schemas.Friendship.FriendshipPayload

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-06
  */
trait FriendshipSupport {
  /**
    *
    * Friendship
    *
    */
  def friendshipAccept(friendshipId: String): Unit

  def friendshipAdd(contactId: String, hello: String): Unit

  def friendshipSearchPhone(phone: String): String

  def friendshipSearchWeixin(weixin: String): String

  protected def friendshipRawPayload(friendshipId: String): FriendshipPayload

}
