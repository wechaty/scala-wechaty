package wechaty.puppet.schemas

object Message {

  object MessageType extends Enumeration {
    type Type = Value
    val Unknown: Type = Value(0)

    val Attachment = Value // Attach(6),
    val Audio = Value // Audio(1), Voice(34)
    val Contact = Value // ShareCard(42)
    val ChatHistory = Value // ChatHistory(19)
    val Emoticon = Value // Sticker: Emoticon(15), Emoticon(47)
    val Image = Value // Img(2), Image(3)
    val Text = Value // Text(1)
    val Location = Value // Location(48)
    val MiniProgram = Value // MiniProgram(33)
    val GroupNote = Value // GroupNote(53)
    val Transfer = Value // Transfers(2000)
    val RedEnvelope = Value // RedEnvelopes(2001)
    val Recalled = Value // Recalled(10002)
    val Url = Value // Url(5)
    val Video = Value // Video(4), Video(43)
  }

  /**
    * Huan(202001): Wechat Server Message Type Value (to be confirmed.)
    */
  object WechatAppMessageType extends Enumeration {
    type Type = Value
    val Text: Type = Value(1)
    val Img: Type = Value(2)
    val Audio: Type = Value(3)
    val Video: Type = Value(4)
    val Url: Type = Value(5)
    val Attach: Type = Value(6)
    val Open: Type = Value(7)
    val Emoji: Type = Value(8)
    val VoiceRemind: Type = Value(9)
    val ScanGood: Type = Value(10)
    val Good: Type = Value(13)
    val Emotion: Type = Value(15)
    val CardTicket: Type = Value(16)
    val RealtimeShareLocation: Type = Value(17)
    val ChatHistory: Type = Value(19)
    val MiniProgram: Type = Value(33)
    val Transfers: Type = Value(2000)
    val RedEnvelopes: Type = Value(2001)
    val ReaderType: Type = Value(100001)
  }

  /**
    * Wechat Server Message Type Value (to be confirmed)
    * Huan(202001): The Windows(PC) DLL match the following numbers.
    */
  object WechatMessageType extends Enumeration {
    type Type = Value
    val Text: Type = Value(1)
    val Image: Type = Value(3)
    val Voice: Type = Value(34)
    val VerifyMsg: Type = Value(37)
    val PossibleFriendMsg: Type = Value(40)
    val ShareCard: Type = Value(42)
    val Video: Type = Value(43)
    val Emoticon: Type = Value(47)
    val Location: Type = Value(48)
    val App: Type = Value(49)
    val VoipMsg: Type = Value(50)
    val StatusNotify: Type = Value(51)
    val VoipNotify: Type = Value(52)
    val VoipInvite: Type = Value(53)
    val MicroVideo: Type = Value(62)
    val Transfer: Type = Value(2000) //  // 转账
    val RedEnvelope: Type = Value(2001) //  // 红包
    val MiniProgram: Type = Value(2002) //  // 小程序
    val GroupInvite: Type = Value(2003) //  // 群邀请
    val File: Type = Value(2004) //  // 文件消息
    val SysNotice: Type = Value(9999)
    val Sys: Type = Value(10000)
    val Recalled: Type = Value(10002) //   // NOTIFY 服务通知
  }

  /** @hidden */
  class MessagePayload {
    var id: String = _

    // use message id to get rawPayload to get those informations when needed
    // contactId?    : string,        // Contact ShareCard
    var mentionIdList: Array[String] = _ //      // Mentioned Contacts' Ids

    var filename: String = _
    var text: String = _
    var timestamp: Number = _ //        // Huan(202001): we support both seconds & milliseconds in Wechaty now.
    var `type`: MessageType.Type = _
//  }

//  /** @hidden */
//  class MessagePayloadRoom extends MessagePayload {
    var fromId: String = _
    var roomId: String = _
    var toId: String = _ //    // if to is not set, then room must be set
//  }
//
//  /** @hidden */
//  class MessagePayloadTo extends MessagePayload {
//    var fromId: String = _
//    var roomId: String = _
//    var toId: String = _ //    // if to is not set, then room must be set
  }


  class MessageQueryFilter {
    var fromId: String = _
    var id: String = _
    var roomId: String = _
    var text: String = _
    var toId: String = _
    var `type`: MessageType.Type = _
  }

  /** @hidden */
  type MessagePayloadFilterFunction = MessagePayload => Boolean

  /** @hidden */
  type MessagePayloadFilterFactory = MessageQueryFilter => MessagePayloadFilterFunction
}
