package wechaty.padplus.schemas
object ModelFriendship {

class PadplusRequestTokenPayload {
  var full_url:String = _
  var info:String = _
  var message:String = _
  var share_url:String = _
  var status:Number = _
}

class PadplusFriendshipPayload {
  var fromusername:String = _ //    // 'lizhuohuan'
  var encryptusername:String = _ //    // v1_xxx@stranger'
  var content:String = _ //    // 'hello'
  var scene:String = _ //    // scene type
  var ticket:String = _ //    // 'v2_1a0d2cf325e64b6f74bed09e944529e7cc7a7580cb323475050664566dd0302d89b8e2ed95b596b459cf762d94a0ce606da39babbae0dc26b18a62e079bfc120@stranger',
}

class FriendshipPayloadBase {
  var id:String = _
  var contactId:String = _
  var hello:String = _
  var timestamp:Number = _
}

class FriendshipPayloadConfirm extends FriendshipPayloadBase  {
//  var type:FriendshipType.Confirm = _
}

class FriendshipPayloadReceive extends FriendshipPayloadBase  {
  var stranger:String = _
  var ticket:String = _
//  var type:FriendshipType.Receive = _
}

class FriendshipPayloadVerify extends FriendshipPayloadBase {
//  var type:FriendshipType.Verify = _
}

//type FriendshipPayload = FriendshipPayloadConfirm | FriendshipPayloadReceive | FriendshipPayloadVerify

class AddContactGrpcResponse {
  var status:String = _
}
 }
