package wechaty.puppet.schemas

object RoomInvitation {

  class RoomInvitationPayload {
    var id: String = _
    var inviterId: String = _
    var topic: String = _
    var avatar: String = _
    var invitation: String = _
    var memberCount: Number = _
    var memberIdList: Array[String] = _ //    // Friends' Contact Id List of the Room
    var timestamp: Number = _ //      // Unix Timestamp, in seconds or milliseconds
    var receiverId: String = _ //      // the room invitation should send to which contact.
  }

}
