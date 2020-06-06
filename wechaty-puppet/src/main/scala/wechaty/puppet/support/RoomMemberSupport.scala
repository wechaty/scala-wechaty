package wechaty.puppet.support

import wechaty.puppet.schemas.Room.RoomMemberPayload

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-06
  */
trait RoomMemberSupport {
  /**
    *
    * RoomMember
    *
    */
  def roomAnnounce(roomId: String): String

  def roomAnnounce(roomId: String, text: String): Unit

  def roomMemberList(roomId: String): Array[String]

  protected def roomMemberRawPayload(roomId: String, contactId: String): RoomMemberPayload
}
