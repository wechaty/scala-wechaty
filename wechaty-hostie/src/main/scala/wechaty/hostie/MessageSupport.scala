package wechaty.hostie

import io.github.wechaty.grpc.puppet.Message.{MessagePayloadRequest, MessageSendTextRequest}
import wechaty.puppet.LoggerSupport
import wechaty.puppet.schemas.Events.EventMessagePayload
import wechaty.puppet.schemas.Message.{MessagePayload, MessageType}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
trait MessageSupport {
  self:LoggerSupport with GrpcSupport =>
  def messageRawPayload(id :String):MessagePayload={
    info("PuppetHostie MessagePayload({})", id)
    val response = grpcClient.messagePayload(MessagePayloadRequest.newBuilder().setId(id).build())
    val messagePayload = new MessagePayload
    messagePayload.id = response.getId
    messagePayload.mentionIdList = response.getMentionIdsList.toArray(Array[String]())
    messagePayload.fileName = response.getFilename
    messagePayload.text = response.getText
    messagePayload.timestamp = response.getTimestamp
    if(response.getTypeValue > MessageType.MessageTypeVideo.id )
      messagePayload.`type` = MessageType.UNRECOGNIZED
    else
      messagePayload.`type` = MessageType.apply(response.getTypeValue)

    messagePayload.fromId = response.getFromId
    messagePayload.roomId = response.getRoomId
    messagePayload.toId = response.getToId

    messagePayload
  }
  def messageSendText(conversationID :String , text :String , mentionIDList:String*)={
    info("PuppetHostie messageSendText(%s, %s)\n", conversationID, text)
    val request = MessageSendTextRequest
      .newBuilder()
      .setConversationId(conversationID)
      .setText(text).build()
    val response = grpcClient.messageSendText(request)
    response.getId
  }
  def toMessagePayload(eventMessagePayload: EventMessagePayload):MessagePayload={
    messageRawPayload(eventMessagePayload.messageId)
  }
}
