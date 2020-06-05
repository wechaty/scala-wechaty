package wechaty.hostie.support

import io.github.wechaty.grpc.puppet.Base.DingRequest
import io.github.wechaty.grpc.puppet.Message.{MessagePayloadRequest, MessageSendTextRequest}
import wechaty.puppet.schemas.Message.{MessagePayload, MessageType}
import wechaty.puppet.{LoggerSupport, Puppet}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
trait MessageRawSupport {
  self:LoggerSupport with GrpcSupport with Puppet=>
  override protected def messageRawPayload(id :String):MessagePayload={
    info("PuppetHostie MessagePayload({})", id)
    val response = grpcClient.messagePayload(MessagePayloadRequest.newBuilder().setId(id).build())
    val messagePayload = new MessagePayload
    messagePayload.id = response.getId
    messagePayload.mentionIdList = response.getMentionIdsList.toArray(Array[String]())
    messagePayload.filename = response.getFilename
    messagePayload.text = response.getText
    messagePayload.timestamp = response.getTimestamp
    if(response.getTypeValue > MessageType.Video.id )
      messagePayload.`type` = MessageType.Unknown
    else
      messagePayload.`type` = MessageType.apply(response.getTypeValue)

    messagePayload.fromId= response.getFromId
    messagePayload.roomId = response.getRoomId
    messagePayload.toId = response.getToId

    messagePayload
  }

  override protected def ding(data: String): Unit = {
    val request = DingRequest.newBuilder().setData(data).build()
    grpcClient.ding(request)
  }

  override def messageSendText(conversationID :String , text :String , mentionIDList:String*):String ={
    info("PuppetHostie messageSendText({}, {})", conversationID, text)
    val request = MessageSendTextRequest
      .newBuilder()
      .setConversationId(conversationID)
      .setText(text).build()
    val response = grpcClient.messageSendText(request)
    response.getId.getValue
  }
}
