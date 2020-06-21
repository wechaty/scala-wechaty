package wechaty.padplus.schemas
object ModelEvent {

class RoomJoinEvent {
  var inviteeNameList:Array[String] = _
  var inviterName:String = _
  var roomId:String = _
  var timestamp:Number = _ //  // Unix Timestamp, in seconds
}

class RoomLeaveEvent {
  var leaverNameList:Array[String] = _
  var removerName:String = _
  var roomId:String = _
  var timestamp:Number = _ //   // Unix Timestamp, in seconds
}

class RoomTopicEvent {
  var changerName:String = _
  var roomId:String = _
  var topic:String = _
  var timestamp:Number = _ //  // Unix Timestamp, in seconds
}

class RoomInviteEvent {
  var fromUser:String = _
  var msgId:String = _
  var receiver:String = _
  var roomName:String = _
  var thumbUrl:String = _
  var timestamp:Number = _ //  // Unix Timestamp, in seconds
  var url:String = _
}
 }
