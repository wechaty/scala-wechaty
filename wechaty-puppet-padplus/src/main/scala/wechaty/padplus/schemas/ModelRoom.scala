package wechaty.padplus.schemas

import wechaty.padplus.schemas.ModelContact.PadplusConversation

object ModelRoom {
class PadplusRoomMemberPayload {
  var contactId:String = _
  var nickName:String = _
  var displayName:String = _
  var bigHeadUrl:String = _
  var smallHeadUrl:String = _
  var inviterId:String = _
}

class PadplusMemberBrief {
  var UserName:String = _
  var NickName:String = _
}

class GrpcRoomMemberPayload {
  var DisplayName:String = _
  var HeadImgUrl:String = _
  var InvitedBy:String = _
  var MemberContactFlag:Number = _
  var NickName:String = _
  var RemarkName:String = _
  var UserName:String = _
}

class GrpcRoomMemberList {
  var roomId:String = _
  var membersJson:String = _
}

class PadplusRoomPayload extends PadplusConversation{
  var alias:String = _
  var bigHeadUrl:String = _
  var chatRoomOwner:String = _
  var chatroomVersion:Number = _
  var contactType:Number = _
  var stranger:String = _
  var members:Array[PadplusMemberBrief] = _
  var tagList:String = _
  var nickName:String = _
  var smallHeadUrl:String = _
  var ticket:String = _
  var chatroomId:String = _
  var memberCount:Number = _
}

class GrpcRoomPayload {
  var ContactType:Number = _
  var ExtInfoExt:String = _
  var Sex:Number = _
  var EncryptUsername:String = _
  var wechatUserName:String = _
  var PYQuanPin:String = _
  var Remark:String = _
  var LabelLists:String = _
  var ChatroomVersion:Number = _
  var ExtInfo:String = _
  var ChatRoomOwner:String = _
  var VerifyFlag:Number = _
  var ContactFlag:Number = _
  var Ticket:String = _
  var UserName:String = _
  var src:Number = _
  var HeadImgUrl:String = _
  var RemarkPYInitial:String = _
  var MsgType:Number = _
  var City:String = _
  var NickName:String = _
  var Province:String = _
  var Alias:String = _
  var Signature:String = _
  var RemarkName:String = _
  var RemarkPYQuanPin:String = _
  var Uin:Number = _
  var SmallHeadImgUrl:String = _
  var PYInitial:String = _
  var Seq:String = _
  var BigHeadImgUrl:String = _
}

class PadplusRoomInvitationPayload {
  var id:String = _
  var fromUser:String = _
  var receiver:String = _
  var roomName:String = _
  var thumbUrl:String = _
  var timestamp:Number = _
  var url:String = _
}

class PadplusRoomInviteEvent {
  var fromUser:String = _
  var msgId:String = _
  var receiver:String = _
  var roomName:String = _
  var timestamp:Number = _
  var thumbUrl:String = _
  var url:String = _
}

class GrpcCreateRoomData {
  var status:Number = _
  var roomId:String = _
  var message:String = _
  var createMessage:Array[GrpcCreateRoomMember] = _
}

class GrpcCreateRoomMember {
  var wxid:String = _
  var status:Number = _
}

class GrpcGetAnnouncementData {
  var annoumcementPublisher:String = _
  var annoumcementPublishTime:Number = _
  var message:String = _
  var status:Number = _
  var announcement:String = _
}

class GrpcSetAnnouncementData {
  var message:String = _
  var content:String = _
  var status:Number = _
}

class PadplusRoomMemberMap {
  var members:Map[String,PadplusRoomMemberPayload] = _
}

class GrpcAccpetRoomInvitation {
  var chatRoomType:String = _
  var cmdid:String = _
  var inviteDetailUrl:String = _
  var inviteFrom:String = _
  var inviteUrl:String = _
  var loginer:String = _
  var queueName:String = _
  var source:String = _
  var uin:String = _
  var userName:String = _
}
 }
