package wechaty.puppet.support

import com.github.benmanes.caffeine.cache.Cache
import wechaty.puppet.Puppet
import wechaty.puppet.schemas.RoomInvitation.RoomInvitationPayload

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-06
  */
trait RoomInvitationSupport {
  self:Puppet =>
  private[puppet] val cacheRoomInvitationPayload = createCache().asInstanceOf[Cache[String, RoomInvitationPayload]]

  def roomInvitationAccept(roomInvitationId: String): Unit

  protected def roomInvitationRawPayload(roomInvitationId: String): RoomInvitationPayload

  def roomInvitationPayload (roomInvitationId: String):RoomInvitationPayload = {
    this.cacheRoomInvitationPayload.get(roomInvitationId,_ =>{
      this.roomInvitationRawPayload(roomInvitationId)
    })
  }
}
