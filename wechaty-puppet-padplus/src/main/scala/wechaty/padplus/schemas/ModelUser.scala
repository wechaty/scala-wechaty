package wechaty.padplus.schemas

import wechaty.padplus.schemas.PadplusEnums.{LoginStatus, QrcodeStatus}

object ModelUser {

class PadplusQrcode {
  var qrcode:String = _
  var qrcodeId:String = _
}

class PadplusQrcodeStatus {
  var headUrl:String = _
  var nickName:String = _
  var status:QrcodeStatus.Type = _
  var userName:String = _
}

class PadplusQrcodeLogin {
  var headImgUrl:String = _
  var nickName:String = _
  var status:LoginStatus.Type = _
  var uin:String = _
  var userName:String = _
  var verifyFlag:String = _
}

class GrpcLoginData {
  var event:String = _
  var head_url:String = _
  var loginer:String = _
  var msg:String = _
  var nick_name:String = _
  var qrcodeId:String = _
  var serverId:String = _
  var status:Number = _
  var user_name:String = _
}

class ScanData {
  var head_url:String = _
  var msg:String = _
  var nick_name:String = _
  var qrcodeId:String = _
  var status:Number = _
  var user_name:String = _
}

class LogoutGrpcResponse {
  var code:Number = _
  var uin:String = _
  var message:String = _
  var mqType:Number = _
}
 }
