package wechaty.padplus.schemas
object ModelTag {
class TagGrpcPayload {
  var LabelID:String = _
  var LabelName:String = _
}

class TagPayload {
  var id:String = _
  var name:String = _
}

class TagNewOrListGrpcResponse {
  var count:Number = _
  var labelList:Array[TagGrpcPayload] = _
  var loginer:String = _
  var message:String = _
  var queueName:String = _
  var status:Number = _
  var uin:String = _
}

class TagNewOrListResponse {
  var count:Number = _
  var tagList:Array[TagGrpcPayload] = _
  var loginer:String = _
  var message:String = _
  var queueName:String = _
  var status:Number = _
  var uin:String = _
}

class TagOtherOperationsGrpcResponse {
  var loginer:String = _
  var message:String = _
  var queueName:String = _
  var status:Number = _
  var uin:String = _
  var userName:String = _
}
 }
