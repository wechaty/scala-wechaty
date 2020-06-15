package wechaty.puppet.schemas

object Friendship {

  object FriendshipType extends Enumeration {
    type Type = Value
    val Unknown: Type = Value(0)
    val Confirm: Type = Value(1)
    val Receive: Type = Value(2)
    val Verify: Type = Value(3)
  }

  /**
    * Huan(202002): Does those numbers are the underlying Wechat Protocol Data Values?
    */
  object FriendshipSceneType extends Enumeration {
    type Type = Value
    val Unknown: Type = Value(0) //    // Huan(202003) added by myself
    val QQ: Type = Value(1) //     // FIXME: Huan(202002) in Wechat PC, QQ = 12.
    val Email: Type = Value(2)
    val Weixin: Type = Value(3)
    val QQtbd: Type = Value(12) //    // FIXME: confirm the two QQ number QQ号搜索
    val Room: Type = Value(14)
    val Phone: Type = Value(15)
    val Card: Type = Value(17) //    // 名片分享
    val Location: Type = Value(18)
    val Bottle: Type = Value(25)
    val Shaking: Type = Value(29)
    val QRCode: Type = Value(30)
  }

  /** @hidden */
  class FriendshipPayload {
    var id: String = _

    var contactId: String = _
    var hello: String = _
    var timestamp: Number = _ //  // Unix Timestamp, in seconds or milliseconds
    var `type`:FriendshipType.Type = _
  }

  /** @hidden */
  class FriendshipPayloadConfirm extends FriendshipPayload {
//    override var `type` = FriendshipType.Confirm
  }

  /** @hidden */
  class FriendshipPayloadReceive extends FriendshipPayload {
    var scene: FriendshipSceneType.Type = _
    var stranger: String = _
    var ticket: String = _
//    override var `type` = FriendshipType.Receive
  }

  /** @hidden */
  class FriendshipPayloadVerify extends FriendshipPayload {
//    override var `type` = FriendshipType.Verify
  }


  class FriendshipSearchCondition {
    var phone: String = _
    var weixin: String = _
  }

}
