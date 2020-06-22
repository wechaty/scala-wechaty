package wechaty.padplus.schemas

import wechaty.padplus.schemas.PadplusEnums.{ContactOperationBitVal, ContactOperationCmdId, GrpcSelfAvatarType, PadplusMessageType, PadplusRoomMemberFlag}
import wechaty.puppet.schemas.Contact.ContactGender


object GrpcSchemas {

class GrpcQrCode {
  var qrcodeId:String = _
  var qrcode:String = _
}

class GrpcQrCodeStatus {
  var head_url:String = _
  var nick_name:String = _
  var status:Number = _
  var user_name:String = _
}

class GrpcQrCodeLogin {
  var alias:String = _
  var headImgUrl:String = _
  var nickName:String = _
  var status:Number = _
  var uin:String = _
  var userName:String = _
  var verifyFlag:String = _
}

class GrpcMessagePayload {
  var AppMsgType:Number = _
  var Content:String = _
  var CreateTime:Number = _
  var fileName:String = _
  var FileName:String = _
  var fromMemberNickName:String = _
  var FromMemberNickName:String = _
  var FromMemberUserName:String = _
  var fromMemberUserName:String = _
  var FromUserName:String = _
  var ImgBuf:String = _
  var ImgStatus:Number = _
  var L1MsgType:Number = _
  var MsgId:String = _
  var MsgSource:String = _
  var msgSourceCd:Number = _
  var MsgType:Int= _
  var NewMsgId:Number = _
  var PushContent:String = _
  var Status:Number = _
  var ToUserName:String = _
  var Uin:String = _
  var Url:String = _
  var url:String = _
  var wechatUserName:String = _
}

class GrpcSelfInfoPayload {
  var alias:String = _ //                            // "",             -> weixin id
  var bindUin:String = _ //                          // 251642490,      -> QQ number
  var msgType:PadplusMessageType.Type = _ //               // 101,
  var signature:String = _ //                        // "",
  var userName:String = _ //                         // "lylezhuifeng", -> unique id
  var nickName:String = _ //                         // "高原ོ",
  var sex:ContactGender.Type = _ //                       // 1,
  var province:String = _ //                         // "Beijing",
  var city:String = _ //                             // "",
  var bindEmail:String = _ //                        // "lylezhuifeng@qq.com",
  var bindMobile:String = _ //                       // "13999999999"
}

class GrpcSelfAvatarPayload {
  var msgType:PadplusMessageType.Type = _ //                       // 35,
  var imgType:GrpcSelfAvatarType.Type = _ //                      // 1,
  var imgLen:Number = _ //                                   // 4218,
  var imgBuf:String = _ //                                   // "/5FQ9qFCup5OcSStjioHU0GNcbDPiSmkusuMq6kqHtQoUoTjVpATIcA7KwKftA0KFZ2WIkou2APWkl/vLohKtwBnFChQXYZE5DJCRUk0TmhQqIguCcUKFCmAf/Z",
  var imgMd5:String = _ //                                   // "0144847978f6667ed59cc3d2b4350eb5",
  var bigHeadImgUrl:String = _ //                            // "http://wx.qlogo.cn/mmhead/KDLS0iaeMdibHvaeoZVaPM/132",
  var smallHeadImgUrl:String = _ //                          // "http://wx.qlogo.cn/mmhead/KDLS0fhbZw1jQScfCqfVaPM/0"
}

class GrpcDeletedPayload {
  var msgType:PadplusMessageType.Type = _
  var userName:String = _
}

//type GrpcSyncMessagePayload = GrpcMessagePayload
//                                   | GrpcRoomRawPayload
//                                   | GrpcSelfInfoPayload
//                                   | GrpcSelfAvatarPayload
//                                   | GrpcDeletedPayload

class GrpcRoomRawPayload {
  var alias:String = _
  var bigHeadImgUrl:String = _
  var chatRoomOwner:String = _
  var chatroomVersion:Number = _
  var contactType:Number = _
  var encryptUsername:String = _
  var extInfo:String = _
  var extInfoExt:String = _
  var tagList:String = _
  var msgType:PadplusMessageType.Type = _
  var nickName:String = _
  var smallHeadImgUrl:String = _
  var ticket:String = _
  var userName:String = _
  var verifyFlag:Number = _
}

class GrpcRoomMemberRawPayload {
  var chatroomUsername:String = _
  var serverVersion:Number = _
  var memberDetails:Array[GrpcRoomMemberDetail] = _
}

class GrpcRoomMemberDetail {
  var userName:String = _
  var nickName:String = _
  var displayName:String = _
  var bigHeadImgUrl:String = _
  var smallHeadImgUrl:String = _
  var chatroomMemberFlag:PadplusRoomMemberFlag.Type = _
  var inviterUserName:String = _
}

class GrpcContactOperationOption {
  var cmdid:ContactOperationCmdId.Type = _
  var userId:String = _
  var bitVal:ContactOperationBitVal.Type = _
  var remark:String = _
}

class GrpcCreateRoomMemberPayload {
  var memberName:String = _
  var memberStatus:Number = _
}

class GrpcCreateRoomPayload {
  var roomeid:String = _
  var members:Array[GrpcCreateRoomMemberPayload] = _
}

class GrpcGetMsgImageType {
  var imageData:String = _
}

class GrpcGetMsgVoiceType {
  var voiceData:String = _
}

class GrpcGetA8KeyType {
  var url:String = _
  var xWechatKey:String = _
  var xWechatUin:String = _
}

class GrpcGetContactQrcodePayload {
  var qrcodeBuf:String = _
  var foterWording:String = _
}

class GrpcGetCdnDnsPayload {
  //TODO to fix
  class dnsCdn{
    var ver:String = _
    var uin:String = _
    var ip:String = _
    var aesKey:String = _
  }
  class snsCdn{
    var ver:String = _
    var uin:String = _
    var ip:String = _
    var aesKey:String = _
  }
  class appCdn{
    var ver:String = _
    var uin:String = _
    var ip:String = _
    var aesKey:String = _
  }
  var clientVersion:Number = _
}

class GrpcLoginDeviceInfo {
  var loginer:String = _
  var uin:String = _
  var userName:String = _
  var nickName:String = _
  var headImgUrl:String = _
  var wechatUserId:String = _
  var deviceInfo:Any = _
  var token:String = _
  var loginType:String = _
  var childId:String = _
}

class LoginDeviceInfo {
  var uin:String = _
  var userName:String = _
  var nickName:String = _
  var headImgUrl:String = _
  var wechatUserId:String = _
  var deviceName:String = _
  var token:String = _
  var loginType:String = _
  var childId:String = _
}

object GRPC_CODE extends Enumeration {
 type Type = Value
  val OK:Type =  Value(0)
  val CANCELLED:Type =  Value(1)
  val UNKNOWN:Type =  Value(2)
  val INVALID_ARGUMENT:Type =  Value(3)
  val DEADLINE_EXCEEDED:Type =  Value(4)
  val NOT_FOUND:Type =  Value(5)
  val ALREADY_EXISTS:Type =  Value(6)
  val PERMISSION_DENIED:Type =  Value(7)
  val UNAUTHENTICATED:Type =  Value(16)
  val RESOURCE_EXHAUSTED:Type =  Value(8)
  val FAILED_PRECONDITION:Type =  Value(9)
  val ABORTED:Type =  Value(10)
  val OUT_OF_RANGE:Type =  Value(11)
  val UNIMPLEMENTED:Type =  Value(12)
  val INTERNAL:Type =  Value(13)
  val UNAVAILABLE:Type =  Value(14)
  val DATA_LOSS:Type =  Value(15)
}
 }
