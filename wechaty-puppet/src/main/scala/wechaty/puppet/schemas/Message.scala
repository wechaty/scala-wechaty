package wechaty.puppet.schemas

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
object Message {

  object MessageType extends Enumeration {
    type Type = Value
    val MessageTypeUnknown: Value = Value(0)
    val MessageTypeAttachment: Value = Value(1)
    val MessageTypeAudio: Value = Value(2)
    val MessageTypeContact: Value = Value(3)
    val MessageTypeChatHistory: Value = Value(4)
    val MessageTypeEmoticon: Value = Value(5)
    val MessageTypeImage: Value = Value(6)
    val MessageTypeText: Value = Value(7)
    val MessageTypeLocation: Value = Value(8)
    val MessageTypeMiniProgram: Value = Value(9)
    val MessageTypeTransfer: Value = Value(10)
    val MessageTypeRedEnvelope: Value = Value(11)
    val MessageTypeRecalled: Value = Value(12)
    val MessageTypeUrl: Value = Value(13)
    val MessageTypeVideo: Value = Value(14)
    val UNRECOGNIZED:Value=Value(-1)
  }


  object WeChatAppMessageType extends Enumeration {
    type Type = Value
    val WeChatAppMessageTypeText: Value = Value(1)
    val WeChatAppMessageTypeImg: Value = Value(2)
    val WeChatAppMessageTypeAudio: Value = Value(3)
    val WeChatAppMessageTypeVideo: Value = Value(4)
    val WeChatAppMessageTypeUrl: Value = Value(5)
    val WeChatAppMessageTypeAttach: Value = Value(6)
    val WeChatAppMessageTypeOpen: Value = Value(7)
    val WeChatAppMessageTypeEmoji: Value = Value(8)
    val WeChatAppMessageTypeVoiceRemind: Value = Value(9)
    val WeChatAppMessageTypeScanGood: Value = Value(10)
    val WeChatAppMessageTypeGood: Value = Value(13)
    val WeChatAppMessageTypeEmotion: Value = Value(15)
    val WeChatAppMessageTypeCardTicket: Value = Value(16)
    val WeChatAppMessageTypeRealtimeShareLocation: Value = Value(17)
    val WeChatAppMessageTypeChatHistory: Value = Value(19)
    val WeChatAppMessageTypeMiniProgram: Value = Value(33)
    val WeChatAppMessageTypeTransfers: Value = Value(2000)
    val WeChatAppMessageTypeRedEnvelopes: Value = Value(2001)
    val WeChatAppMessageTypeReaderType: Value = Value(100001)
  }

  object WeChatMessageType extends Enumeration {
    type Type = Value
    val WeChatMessageTypeText: Value = Value(1)
    val WeChatMessageTypeImage: Value = Value(3)
    val WeChatMessageTypeVoice: Value = Value(34)
    val WeChatMessageTypeVerifyMsg: Value = Value(37)
    val WeChatMessageTypePossibleFriendMsg: Value = Value(40)
    val WeChatMessageTypeShareCard: Value = Value(42)
    val WeChatMessageTypeVideo: Value = Value(43)
    val WeChatMessageTypeEmoticon: Value = Value(47)
    val WeChatMessageTypeLocation: Value = Value(48)
    val WeChatMessageTypeApp: Value = Value(49)
    val WeChatMessageTypeVOIPMsg: Value = Value(50)
    val WeChatMessageTypeStatusNotify: Value = Value(51)
    val WeChatMessageTypeVOIPNotify: Value = Value(52)
    val WeChatMessageTypeVOIPInvite: Value = Value(53)
    val WeChatMessageTypeMicroVideo: Value = Value(62)
    val WeChatMessageTypeTransfer: Value = Value(2000) // 转账)
    val WeChatMessageTypeRedEnvelope: Value = Value(2001) // 红包)
    val WeChatMessageTypeMiniProgram: Value = Value(2002) // 小程序)
    val WeChatMessageTypeGroupInvite: Value = Value(2003) // 群邀请)
    val WeChatMessageTypeFile: Value = Value(2004) // 文件消息)
    val WeChatMessageTypeSysNotice: Value = Value(9999)
    val WeChatMessageTypeSys: Value = Value(10000)
    val WeChatMessageTypeRecalled: Value = Value(10002)
  }

  class MessagePayloadBase {
    var id: String = _

    // use message id to get rawPayload to get those informations when needed
    // contactId     string        // Contact ShareCard
    var mentionIdList: Array[String] = _ // Mentioned Contacts' Ids

    var fileName: String = _
    var text: String = _
    var timestamp: Long = _
    var `type`: MessageType.Type = _
  }

  trait MessagePayloadRoomTrait {
    var fromId: String = _
    var roomId: String = _
    var toId: String = _
  }

  class MessagePayloadRoom extends MessagePayloadRoomTrait {
  }

  type MessagePayloadTo = MessagePayloadRoom

  class MessagePayload extends MessagePayloadBase with MessagePayloadRoomTrait

}
