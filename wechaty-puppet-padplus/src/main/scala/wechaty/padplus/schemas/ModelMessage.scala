package wechaty.padplus.schemas

import wechaty.padplus.schemas.PadplusEnums.{GrpcVoiceFormat, PadplusEmojiType, PadplusMessageType, WechatAppMessageType}

object ModelMessage {

class PadplusRichMediaData {
  var content:String = _
  var msgType:Number = _
  var contentType:String = _
  var src:String = _
  var appMsgType:Number = _
  var fileName:String = _
  var msgId:String = _
  var createTime:Number = _
  var fromUserName:String = _
  var toUserName:String = _
}

class PadplusMessageSource {
  var silence:Boolean = _
  var memberCount:Number = _
  var imageFileName:String = _
  var atUserList:Array[String] = _
}

class GrpcResponseMessageData {
  var msgId:String = _
  var timestamp:Number = _
  var success:Boolean = _
}

class PadplusMessagePayload {
  var appMsgType:Number = _
  var content:String = _
  var createTime:Number = _
  var fileName:String = _
  var fromMemberNickName:String = _
  var fromMemberUserName:String = _
  var fromUserName:String = _
  var imgBuf:String = _
  var imgStatus:Number = _
  var l1MsgType:Number = _
  var msgId:String = _
  var msgSource:String = _
  var msgSourceCd:Number = _
  var msgType:PadplusMessageType.Type = _
  var newMsgId:Number = _
  var pushContent:String = _
  var status:Number = _
  var toUserName:String = _
  var uin:String = _
  var url:String = _
  var wechatUserName:String = _
}

class PadplusAppMessagePayload {
  var des:String = _
  var thumburl:String = _
  var title:String = _
  var url:String = _
  var appattach:PadplusAppAttachPayload = _
  var `type`:WechatAppMessageType.Type = _
  var md5:String = _
  var fromusername:String = _
  var recorditem:String = _
}

class PadplusAppAttachPayload {
  var totallen:Number = _
  var attachid:String = _
  var emoticonmd5:String = _
  var fileext:String = _
  var cdnattachurl:String = _
  var aeskey:String = _
  var cdnthumbaeskey:String = _
  var encryver:Number = _
  var islargefilemsg:Number = _
}

class PadplusEmojiMessagePayload {
  var cdnurl:String = _
  var `type`:PadplusEmojiType.Type = _
  var len:Number = _
  var width:Number = _
  var height:Number = _
}

class PadplusImageMessagePayload {
  var aesKey:String = _
  var encryVer:Number = _
  var cdnThumbAesKey:String = _
  var cdnThumbUrl:String = _
  var cdnThumbLength:Number = _
  var cdnThumbHeight:Number = _
  var cdnThumbWidth:Number = _
  var cdnMidHeight:Number = _
  var cdnMidWidth:Number = _
  var cdnHdHeight:Number = _
  var cdnHdWidth:Number = _
  var cdnMidImgUrl:String = _
  var length:Number = _
  var cdnBigImgUrl:String = _
  var hdLength:Number = _
  var md5:String = _
}

class PadplusRecalledMessagePayload {
  var session:String = _
  var msgId:String = _
  var newMsgId:String = _
  var replaceMsg:String = _
}

class PadplusVoiceMessagePayload {
  var endFlag:Number = _
  var length:Number = _
  var voiceLength:Number = _
  var clientMsgId:String = _
  var fromUsername:String = _
  var downCount:Number = _
  var cancelFlag:Number = _
  var voiceFormat:GrpcVoiceFormat.Type = _
  var forwardFlag:Number = _
  var bufId:Number = _
}

class PadplusLocationMessagePayload {
  var x:Number = _
  var y:Number = _
  var scale:Number = _
  var mapType:String = _
  var label:String = _
  var poiId:String = _
  var poiName:String = _
  var fromUsername:String = _
}

class PadplusVideoMessagePayload {
  var aesKey:String = _
  var cdnThumbAesKey:String = _
  var cdnVideoUrl:String = _
  var cdnThumbUrl:String = _
  var length:Number = _
  var playLength:Number = _
  var cdnThumbLength:Number = _
  var cdnThumbWidth:Number = _
  var cdnThumbHeight:Number = _
  var fromUsername:String = _
  var md5:String = _
  var newMd5:String = _
  var isAd:Boolean = _
}

class PadplusUrlLink {
  var description:String = _
  var thumbnailUrl:String = _
  var title:String = _
  var url:String = _
}

class PadplusMediaData {
  var content:String = _
  var msgId:String = _
  var src:String = _
  var status:String = _
  var thumb:String = _
}

class PadplusRecallData {
  class BaseResponse{
    var Ret:Number = _
    var ErrMsg:String = _
  }
}

class VideoContent {
  var cdnthumbheight:Number = _
  var cdnthumbwidth:Number = _
  var playlength:Number = _
  var thumb:String = _
  var url:String = _
}

class MiniProgramParamsPayload {
  var aeskey:String = _
  var appid:String = _
  var cdnthumbaeskey:String = _
  var cdnthumbheight:Number = _
  var cdnthumblength:Number = _
  var cdnthumburl:String = _
  var cdnthumbwidth:Number = _
  var description:String = _
  var pagepath:String = _
  var sourcedisplayname:String = _
  var sourceusername:String = _
  var title:String = _
  var `type`:Number = _
  var url:String = _
  var username:String = _
  var version:String = _
  var weappiconurl:String = _
}
 }
