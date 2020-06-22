package wechaty.padplus.support

import wechaty.padplus.schemas.PadplusEnums.PadplusMessageType
import wechaty.puppet.schemas.Message.MessageType
import wechaty.puppet.schemas.Puppet._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-22
  */
trait PadplusHelper {
  def messageType (rawType: PadplusMessageType.Type): MessageType.Type = {
    rawType match {

      case PadplusMessageType.Text => MessageType.Text
      case PadplusMessageType.Image => MessageType.Image // console.log(rawPayload)
      case PadplusMessageType.Voice => MessageType.Audio // console.log(rawPayload)
      case PadplusMessageType.Emoticon => MessageType.Emoticon
      case PadplusMessageType.App => MessageType.Attachment
      case PadplusMessageType.Location => MessageType.Location
      case PadplusMessageType.Video => MessageType.Video // console.log(rawPayload)
      case PadplusMessageType.Sys => MessageType.Unknown
      case PadplusMessageType.ShareCard => MessageType.Contact
      case PadplusMessageType.VoipMsg => MessageType.Unknown
      case PadplusMessageType.Recalled => MessageType.Recalled
      case PadplusMessageType.StatusNotify | PadplusMessageType.SysNotice => MessageType.Unknown
      case other =>
        throw new Error("unsupported type: " + other + '(' + rawType + ')')
    }
  }
  def isRoomId (id: String): Boolean ={
    if (isBlank(id)) {
      return false
    }
    "@chatroom$".r.findFirstMatchIn(id).isDefined
  }

  def isContactId (id: String): Boolean = !isRoomId(id)

  def isContactOfficialId (id: String): Boolean ={
    if (isBlank(id)) {
    return false
  }
    "(?i)^gh_".r.findFirstMatchIn(id).isDefined
  }
  def isStrangerV1 (strangerId: String): Boolean ={
    if (isBlank(strangerId)) {
    return false
  }
    "(?i)^v1_".r.findFirstMatchIn(strangerId).isDefined
  }

  def isStrangerV2 (strangerId: String): Boolean = {
    if (isBlank(strangerId)) {
    return false
  }
    "(?i)^v2_".r.findFirstMatchIn(strangerId).isDefined
  }
}
