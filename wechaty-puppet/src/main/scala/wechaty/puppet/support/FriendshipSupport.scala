package wechaty.puppet.support

import com.github.benmanes.caffeine.cache.Cache
import wechaty.puppet.Puppet
import wechaty.puppet.schemas.Friendship.FriendshipPayload

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-06
  */
trait FriendshipSupport {
  self : Puppet =>
  private[puppet] val cacheFriendshipPayload = createCache().asInstanceOf[Cache[String, FriendshipPayload]]

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

  def friendshipPayload (friendshipId : String, newPayloadOpt  : Option[FriendshipPayload] = None): FriendshipPayload = {
    newPayloadOpt match {
      case Some(newPayload) =>
        cacheFriendshipPayload.put(friendshipId, newPayload)
        newPayload
      case _ =>

        /**
          * 1. Try to get from cache first
          */
        val cachedPayload = this.cacheFriendshipPayload.getIfPresent(friendshipId)
        if (cachedPayload != null) {
          cachedPayload
        }
        else {
          /**
            * 2. Cache not found
            */
          val rawPayload = friendshipRawPayload(friendshipId)
          this.cacheFriendshipPayload.put(friendshipId, rawPayload)
          rawPayload
        }
    }
  }
}
