package wechaty.puppet.support

import wechaty.puppet.schemas.RoomInvitation.RoomInvitationPayload

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-06
  */
trait RoomInvitationSupport {
  def roomInvitationAccept (roomInvitationId: String): Unit
  protected def roomInvitationRawPayload (roomInvitationId: String) :  RoomInvitationPayload
}
