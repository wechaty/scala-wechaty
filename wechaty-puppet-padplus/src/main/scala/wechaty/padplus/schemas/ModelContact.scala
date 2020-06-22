package wechaty.padplus.schemas

import wechaty.puppet.schemas.Contact.ContactGender

object ModelContact {

class PadplusContactPayload {
  var alias:String = _
  var contactType:Number = _
  var tagList:String = _
  var bigHeadUrl:String = _ //                      // "http://wx.qlogo.cn/mmhead/ver_1/xfCMmibHH74xGLoyeDFJadrZXX3eOEznPefiaCa3iczxZGMwPtDuSbRQKx3Xdm18un303mf0NFia3USY2nO2VEYILw/0",
  var city:String = _ //                      // 'Haidian'
  var country:String = _ //                      // "CN"
  var nickName:String = _ //                      // "梦君君", Contact: 用户昵称， Room: 群昵称
  var province:String = _ //                      // "Beijing",
  var remark:String = _ //                      // "女儿",
  var sex:ContactGender.Type = _
  var signature:String = _ //                      // "且行且珍惜",
  var smallHeadUrl:String = _
  var stranger:String = _ //                      // 用户v1码，从未加过好友则为空 "v1_0468f2cd3f0efe7ca2589d57c3f9ba952a3789e41b6e78ee00ed53d1e6096b88@stranger"
  var ticket:String = _ //                      // 用户v2码，如果非空则为单向好友(非对方好友) 'v2_xxx@stranger'
  var userName:String = _ //                      // "mengjunjun001" | "qq512436430" Unique name
  var verifyFlag:Number = _
  var contactFlag:Number = _
}

class GrpcContactPayload {
  var Alias:String = _
  var BigHeadImgUrl:String = _
  var ChatRoomOwner:String = _
  var ChatroomVersion:Number = _
  var City:String = _
  var ContactFlag:Number = _
  var ContactType:String = _
  var EncryptUsername:String = _
  var ExtInfo:String = _
  var ExtInfoExt:String = _
  var HeadImgUrl:String = _
  var LabelLists:String = _
  var MsgType:Number = _
  var NickName:String = _
  var Province:String = _
  var PYInitial:String = _
  var PYQuanPin:String = _
  var Remark:String = _
  var RemarkName:String = _
  var RemarkPYInitial:String = _
  var RemarkPYQuanPin:String = _
  var Seq:String = _
  var Sex:Number = _
  var Signature:String = _
  var SmallHeadImgUrl:String = _
  var Type7:String = _
  var Uin:Number = _
  var UserName:String = _
  var VerifyFlag:Number = _
  var wechatUserName:String = _
}

class GrpcSearchContact {
  var avatar:String = _
  var v1:String = _
  var v2:String = _
  var searchId:String = _
  var nickName:String = _
  var wxid:String = _
  var message:String = _
  var status:String = _
}

class GrpcDeleteContact {
  var field:String = _
  var loginer:String = _
  var mqType:Number = _
  var source:String = _
  var uin:String = _
  var userName:String = _
}

class ContactQrcodeGrpcResponse {
  var status:Number = _
  var message:String = _
  var loginer:String = _
  var uin:String = _
  var userName:String = _
  var queueName:String = _
  var qrcodeBuf:String = _
  var style:Number = _
}

class SetContactSelfInfoGrpcResponse {
  var status:Number = _
  var message:String = _
  var loginer:String = _
  var uin:String = _
  var userName:String = _
  var queueName:String = _
  var updateData:ContactSelfUpdateInfo = _
}

class ContactSelfUpdateInfo {
  var nickName:String = _
  var sex:Number = _
  var area:String = _
  var signature:String = _
}

class GetContactSelfInfoGrpcResponse {
  var alias:String = _
  var bigHeadImg:String = _
  var bindEmail:String = _
  var bindMobile:String = _
  var bindQQ:Number = _
  var bytes:String = _
  var city:String = _
  var country:String = _
  var loginer:String = _
  var message:String = _
  var nickName:String = _
  var province:String = _
  var queueName:String = _
  var sex:Int= _
  var signature:String = _
  var smallHeadImg:String = _
  var snsBGImg:String = _
  var status:Number = _
  var uin:String = _
  var userName:String = _
}
 }
