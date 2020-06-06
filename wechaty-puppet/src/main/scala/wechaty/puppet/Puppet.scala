package wechaty.puppet

import com.github.benmanes.caffeine.cache.Caffeine
import wechaty.puppet.support._

/**
  * abstract puppet interface
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-03
  */
trait Puppet extends MessageSupport
  with ContactSupport
  with ContactSelfSupport
  with TagSupport
  with FriendshipSupport
  with RoomInvitationSupport
  with RoomSupport
  with RoomMemberSupport
{
  self:LoggerSupport =>

  protected def createCache()= {
    //TODO optimize lru cache
    Caffeine.newBuilder().maximumSize(100).build()
  }

  def selfIdOpt():Option[String]
}
