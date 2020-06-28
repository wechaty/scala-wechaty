package wechaty.user

import wechaty.Wechaty.PuppetResolver

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-28
  */
class RoomInvitation(id:String)(implicit  resolver:PuppetResolver) {
  def accept(): Unit = {
    resolver.puppet.roomInvitationAccept(this.id)
  }

  def payload={
    resolver.puppet.roomInvitationPayload(this.id)
  }
}
