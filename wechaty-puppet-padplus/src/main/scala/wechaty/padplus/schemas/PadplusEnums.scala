package wechaty.padplus.schemas
object PadplusEnums {
object QrcodeStatus extends Enumeration {
 type Type = Value
  val Waiting:Type =  Value(0)
  val Scanned:Type =  Value(1)
  val Confirmed:Type =  Value(2)
  val Canceled:Type =  Value(4)
  val Expired:Type =  Value(3)
}

object LoginStatus extends Enumeration {
 type Type = Value
  val Logined:Type =  Value(1)
}

object ContactType extends Enumeration {
 type Type = Value
  val Unknown:Type =  Value(0)
  val Personal:Type =  Value(99990)
  val Official:Type =  Value(99991)
}

object FriendshipType extends Enumeration {
 type Type = Value
  val Unknown:Type =  Value(0)
  val Confirm:Type =  Value(99990)
  val Receive:Type =  Value(99991)
  val Verify:Type =  Value(99992)
}

object CheckQRCodeStatus extends Enumeration {
 type Type = Value
  val Ignore:Type =  Value(-2)
  val Unknown:Type =  Value(-1)
  val WaitScan:Type =  Value(0)
  val WaitConfirm:Type =  Value(1)
  val Confirmed:Type =  Value(2)
  val Timeout:Type =  Value(3)
  val Cancel:Type =  Value(4)
}

object RoomAddTypeStatus extends Enumeration {
 type Type = Value
  val Done:Type =  Value(0)
  val NeedInvite:Type =  Value(-2012)
  val InviteConfirm:Type =  Value(-2028)
}

/**
 * Raw type info:
 * see more inhttps://ymiao.oss-cn-shanghai.aliyuncs.com/apifile.txt
 * 2  - 通过搜索邮箱
 * 3  - 通过微信号搜索
 * 5  - 通过朋友验证消息
 * 7  - 通过朋友验证消息(可回复)
 * 12 - 通过QQ好友添加
 * 14 - 通过群来源
 * 15 - 通过搜索手机号
 * 16 - 通过朋友验证消息
 * 17 - 通过名片分享
 * 22 - 通过摇一摇打招呼方式
 * 25 - 通过漂流瓶
 * 30 - 通过二维码方式
 */
object SearchContactTypeStatus extends Enumeration {
 type Type = Value
  val CONTACT:Type =  Value(17) //    // search by contact card
  val EMAIL:Type =  Value(2) //     // search by email
  val FLOAT:Type =  Value(25) //    // search by float bottle
  val MOBILE:Type =  Value(15) //    // search by mobile number
  val QQ:Type =  Value(12) //    // search by qq friend
  val QRCODE:Type =  Value(30) //    // search by scanning qrcode
  val ROOM:Type =  Value(14) //    // search by room
  val Searchable:Type =  Value(0)
  val SHAKE:Type =  Value(22) //    // search by shake and shack
  val UnSearchable:Type =  Value(-24)
  val VERIFY:Type =  Value(16) //    // search friend verify
  val VERIFY_NOREPLY:Type =  Value(5) //     // search by friend verify without reply(朋友验证消息)
  val VERIFY_REPLY:Type =  Value(7) //     // search by friend verify(朋友验证消息，可回复)
  val WXID:Type =  Value(3) //     // search by wxid
}

object PadplusMessageStatus extends Enumeration {
 type Type = Value
  val One:Type =  Value(1)
}

object PadplusStatus extends Enumeration {
 type Type = Value
  val One:Type =  Value(1)
}

object PadplusContinue extends Enumeration {
 type Type = Value
  val Done:Type =  Value(0) //    // Load Ready
  val Go:Type =  Value(1) //    // NOT Load Ready
}

object PadplusPayloadType extends Enumeration {
 type Type = Value
  val ExpirePadplusToken:Type =  Value(-1113) //  // -1113 when the token is expired
  val InvalidPadplusToken:Type =  Value(-1111) //  // -1111 when the token pass to Padplus server is invalid
  val Logout:Type =  Value(-1) //  // -1 when logout
  val OnlinePadplusToken:Type =  Value(-1112) //  // -1112 when the token has already logged in to wechaty
}

object WechatAppMessageType extends Enumeration {
 type Type = Value
  val Text:Type =  Value(1)
  val Img:Type =  Value(2)
  val Audio:Type =  Value(3)
  val Video:Type =  Value(4)
  val Url:Type =  Value(5)
  val Attach:Type =  Value(6)
  val Open:Type =  Value(7)
  val Emoji:Type =  Value(8)
  val VoiceRemind:Type =  Value(9)
  val ScanGood:Type =  Value(10)
  val Good:Type =  Value(13)
  val Emotion:Type =  Value(15)
  val CardTicket:Type =  Value(16)
  val RealtimeShareLocation:Type =  Value(17)
  val ChatHistory:Type =  Value(19)
  val MiniProgram:Type =  Value(33)
  val MiniProgramApp:Type =  Value(36) //   // this is forwardable mini program
  val GroupNote:Type =  Value(53)
  val Transfers:Type =  Value(2000)
  val RedEnvelopes:Type =  Value(2001)
  val ReaderType:Type =  Value(100001)
}

object PadplusEmojiType extends Enumeration {
 type Type = Value
  val Unknown:Type =  Value(0)
  val Static:Type =  Value(1) //     // emoji that does not have animation
  val Dynamic:Type =  Value(2) //     // emoji with animation
}

/**
 * Enum for MsgType values.
 * @enum {number}
 * @property {number} TEXT                - MsgType.TEXT                (1)     for TEXT
 * @property {number} IMAGE               - MsgType.IMAGE               (3)     for IMAGE
 * @property {number} VOICE               - MsgType.VOICE               (34)    for VOICE
 * @property {number} VERIFYMSG           - MsgType.VERIFYMSG           (37)    for VERIFYMSG
 * @property {number} POSSIBLEFRIEND_MSG  - MsgType.POSSIBLEFRIEND_MSG  (40)    for POSSIBLEFRIEND_MSG
 * @property {number} SHARECARD           - MsgType.SHARECARD           (42)    for SHARECARD
 * @property {number} VIDEO               - MsgType.VIDEO               (43)    for VIDEO
 * @property {number} EMOTICON            - MsgType.EMOTICON            (47)    for EMOTICON
 * @property {number} LOCATION            - MsgType.LOCATION            (48)    for LOCATION
 * @property {number} APP                 - MsgType.APP                 (49)    for APP         | File, Media Link
 * @property {number} VOIPMSG             - MsgType.VOIPMSG             (50)    for VOIPMSG
 * @property {number} STATUSNOTIFY        - MsgType.STATUSNOTIFY        (51)    for STATUSNOTIFY
 * @property {number} VOIPNOTIFY          - MsgType.VOIPNOTIFY          (52)    for VOIPNOTIFY
 * @property {number} VOIPINVITE          - MsgType.VOIPINVITE          (53)    for VOIPINVITE
 * @property {number} MICROVIDEO          - MsgType.MICROVIDEO          (62)    for MICROVIDEO
 * @property {number} SYSNOTICE           - MsgType.SYSNOTICE           (9999)  for SYSNOTICE
 * @property {number} SYS                 - MsgType.SYS                 (10000) for SYS         | Change Room Topic, Invite into Room, Kick Off from the room
 * @property {number} RECALLED            - MsgType.RECALLED            (10002) for RECALLED
 */
object PadplusMessageType extends Enumeration {
 type Type = Value
  val Text:Type =  Value(1)
  val Contact:Type =  Value(2)
  val Image:Type =  Value(3)
  val Deleted:Type =  Value(4)
  val Voice:Type =  Value(34)
  val SelfAvatar:Type =  Value(35)
  val VerifyMsg:Type =  Value(37)
  val PossibleFriendMsg:Type =  Value(40)
  val ShareCard:Type =  Value(42)
  val Video:Type =  Value(43)
  val Emoticon:Type =  Value(47)
  val Location:Type =  Value(48)
  val App:Type =  Value(49)
  val VoipMsg:Type =  Value(50)
  val StatusNotify:Type =  Value(51)
  val VoipNotify:Type =  Value(52)
  val VoipInvite:Type =  Value(53)
  val MicroVideo:Type =  Value(62)
  val SelfInfo:Type =  Value(101)
  val SysNotice:Type =  Value(9999)
  val Sys:Type =  Value(10000)
  val Recalled:Type =  Value(10002)
  val N11_2048:Type =  Value(2048) //   // 2048 = 1 << 11
  val N15_32768:Type =  Value(32768) //  // 32768  = 1 << 15
}

// TODO: figure out the meaning of the enum values
object PadplusRoomMemberFlag extends Enumeration {
 type Type = Value
  val Zero:Type =  Value(0)
  val One:Type =  Value(1)
  val Eight:Type =  Value(8)
}

object ContactOperationCmdId extends Enumeration {
 type Type = Value
  val Delete:Type =  Value(7)
  val Operation:Type =  Value(2)
}

object ContactOperationBitVal extends Enumeration {
 type Type = Value
  val SaveToContact:Type =  Value(2051)
  val RemoveFromContact:Type =  Value(2)
  val Star:Type =  Value(71)
  val UnStar:Type =  Value(7)
  val Remark:Type =  Value(7)
  val BlackList:Type =  Value(15)
  val UnBlackList:Type =  Value(7)
}

object GrpcVoiceFormat extends Enumeration {
 type Type = Value
  val Amr:Type =  Value(1)
  val Mp3:Type =  Value(2)
  val Wave:Type =  Value(3)
  val Silk:Type =  Value(4)
}

object GrpcA8KeyScene extends Enumeration {
 type Type = Value
  val ContactOrRoom:Type =  Value(2)
  val HistoryReading:Type =  Value(3)
  val QRCodeLink:Type =  Value(4)
  val OAAccount:Type =  Value(7)
}

object AutoLoginError extends Enumeration {
 type Type = Value
  val CALL_FAILED:Type =  Value//('CALL_FAILED')
  val LOGIN_ERROR:Type =  Value//('LOGIN_ERROR')
}

object EncryptionServiceError extends Enumeration {
 type Type = Value
  val NO_SESSION:Type =  Value//('NO_SESSION')
  val INTERNAL_ERROR:Type =  Value//('INTERNAL_ERROR')
}

object GrpcSelfAvatarType extends Enumeration {
 type Type = Value
  val CURRENT:Type =  Value(1)
  val OLD:Type =  Value(2)
}

object CDNFileType extends Enumeration {
 type Type = Value
  val IMAGE:Type =  Value(1)
  val MID_IMAGE:Type =  Value(2)
  val VIDEO_THUMBNAIL:Type =  Value(3)
  val VIDEO:Type =  Value(4)
  val ATTACHMENT:Type =  Value(5)
}

object CDNFileMd5Exist extends Enumeration {
 type Type = Value
  val NON_EXIST:Type =  Value(0)
  val EXIST:Type =  Value(1)
}

object PadplusErrorType extends Enumeration {
 type Type = Value
  val LOGIN:Type =  Value//('LOGIN')
  val NO_ID:Type =  Value//('NO_ID')
  val NO_CACHE:Type =  Value//('NO_CACHE')
  val EXIT:Type =  Value//('EXIT')
}

object PadplusAutoLoginErrorType extends Enumeration {
 type Type = Value
  val SELF_LOGOUT:Type =  Value//('SELF_LOGOUT')
  val TOO_FREQUENT:Type =  Value//('TOO_FREQUENT')
  val LOGIN_ANOTHER_DEVICE:Type =  Value//('LOGIN_ANOTHER_DEVICE')
  val LOGIN_ANOTHER_DEVICE_WITH_WARN:Type =  Value//('LOGIN_ANOTHER_DEVICE_WITH_WARN')
  val SAFETY_LOGOUT:Type =  Value//('SAFETY_LOGOUT')
  val UNKNOWN:Type =  Value//('UNKNOWN')
}

object MessageSendType extends Enumeration {
 type Type = Value
  val SELF_SENT:Type =  Value(1)
  val CONTACT_SENT:Type =  Value(2)
}

object RequestStatus extends Enumeration {
 type Type = Value
  val Fail=Value(0)
  val Success=Value(1)
}

object GrpcMQType extends Enumeration {
 type Type = Value
  val RECEIVE_MESSAGE:Type =  Value(2)
  val CONTACT_INFO_CHANGE:Type =  Value(3)
  val DELETE_CONTACT:Type =  Value(4)
  val GET_ROOM_MEMBER:Type =  Value(5)
  val ROOM_MEMBER_CHANGE:Type =  Value(6)
  val GET_CONTACT:Type =  Value(7)
  val ADD_CONTACT:Type =  Value(15)
  val SYNC:Type =  Value(51)
  val LOGOUT:Type =  Value(1100)
}
 }
