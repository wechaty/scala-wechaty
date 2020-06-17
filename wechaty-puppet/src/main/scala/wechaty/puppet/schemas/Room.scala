package wechaty.puppet.schemas

object Room {

  class RoomMemberQueryFilter {
    var name: String = _
    var roomAlias: String = _
    var contactAlias: String = _
  }

  /**
    * select room function
    */
  type RoomQueryFilter = RoomPayload => Boolean

  class RoomPayload {
    var id: String = _

    var topic: String = _
    var avatar: String = _
    var memberIdList: Array[String] = _
    var ownerId: String = _
    var adminIdList: Array[String] = _
  }

  class RoomMemberPayload {
    var id: String = _
    var roomAlias: String = _ //    // "李佳芮-群里设置的备注", `chatroom_nick_name`
    var inviterId: String = _ //    // "wxid_7708837087612",
    var avatar: String = _
    var name: String = _
  }

  /** @hidden */
  type RoomPayloadFilterFunction = RoomPayload => Boolean

  /** @hidden */
  type RoomPayloadFilterFactory = RoomQueryFilter => RoomPayloadFilterFunction
}
