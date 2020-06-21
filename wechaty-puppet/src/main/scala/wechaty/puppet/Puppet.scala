package wechaty.puppet

import com.typesafe.scalalogging.LazyLogging
import wechaty.puppet.events.EventEmitter
import wechaty.puppet.support._

/**
  * abstract puppet interface
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-03
  */
trait Puppet extends MessageSupport
  with CacheSupport
  with EventEmitter
  with ContactSupport
  with ContactSelfSupport
  with TagSupport
  with FriendshipSupport
  with RoomInvitationSupport
  with RoomSupport
  with RoomMemberSupport
{
  self:LazyLogging =>


  //only for test
  private[wechaty] def clearAllCache(): Unit = {
    cacheContactPayload.cleanUp()
    cacheFriendshipPayload.cleanUp()
    cacheMessagePayload.cleanUp()
    cacheRoomMemberPayload.cleanUp()
    cacheRoomPayload.cleanUp()
  }

  def selfIdOpt():Option[String]
}
