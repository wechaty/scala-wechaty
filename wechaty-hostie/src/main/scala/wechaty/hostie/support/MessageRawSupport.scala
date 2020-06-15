package wechaty.hostie.support

import io.github.wechaty.grpc.puppet.Base.DingRequest
import io.github.wechaty.grpc.puppet.Message
import io.github.wechaty.grpc.puppet.Message.{MessagePayloadRequest, MessageSendTextRequest}
import wechaty.puppet.schemas.Message.{MessagePayload, MessageType}
import wechaty.puppet.schemas.MiniProgram.MiniProgramPayload
import wechaty.puppet.schemas.Puppet
import wechaty.puppet.schemas.Puppet.objectMapper
import wechaty.puppet.schemas.UrlLink.UrlLinkPayload
import wechaty.puppet.support.MessageSupport
import wechaty.puppet.{LoggerSupport, ResourceBox}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
trait MessageRawSupport {
  self: LoggerSupport with GrpcSupport with MessageSupport =>
  /**
    * message
    */
  override def messageContact(messageId: String): String = {
    val request = Message.MessageContactRequest.newBuilder()
      .setId(messageId)
      .build()
    val response = grpcClient.messageContact(request)
    response.getId
  }

  override def messageMiniProgram(messageId: String): MiniProgramPayload = {
    val request = Message.MessageMiniProgramRequest.newBuilder()
      .setId(messageId)
      .build()

    val response = grpcClient.messageMiniProgram(request)
    val miniProgram = response.getMiniProgram
    Puppet.objectMapper.readValue(miniProgram, classOf[MiniProgramPayload])
  }

  override def messageUrl(messageId: String): UrlLinkPayload = {
    val request = Message.MessageUrlRequest.newBuilder()
      .setId(messageId)
      .build()

    val response = grpcClient.messageUrl(request)
    val urlLink = response.getUrlLink
    Puppet.objectMapper.readValue(urlLink, classOf[UrlLinkPayload])

  }

  override def messageSendContact(conversationId: String, contactId: String): String = {
    val request = Message.MessageSendContactRequest.newBuilder()
      .setContactId(contactId)
      .setConversationId(conversationId)
      .build()


    val response = grpcClient.messageSendContact(request)
    response.getId.getValue
  }

  override def messageSendFile(conversationId: String, file: ResourceBox): String = {
    val fileJson = file.toJson()

    val request = Message.MessageSendFileRequest.newBuilder()
      .setConversationId(conversationId)
      .setFilebox(fileJson)
      .build()

    val response = grpcClient.messageSendFile(request)
    response.getId.getValue
  }

  override def messageSendMiniProgram(conversationId: String, miniProgramPayload: MiniProgramPayload): String = {
    val request = Message.MessageSendMiniProgramRequest.newBuilder()
      .setConversationId(conversationId)
      .setMiniProgram(objectMapper.writeValueAsString(miniProgramPayload))
      .build()

    val response = grpcClient.messageSendMiniProgram(request)
    response.getId.getValue
  }

  override def messageSendText(conversationId: String, text: String, mentionIdList: Array[String]): String = {
    val request = Message.MessageSendTextRequest.newBuilder()
      .setConversationId(conversationId)
      .setText(text)
      .build()

    val response = grpcClient.messageSendText(request)
    response.getId.getValue
  }

  override def messageSendUrl(conversationId: String, urlLinkPayload: UrlLinkPayload): String = {
    val request = Message.MessageSendUrlRequest.newBuilder()
      .setConversationId(conversationId)
      .setUrlLink(objectMapper.writeValueAsString(urlLinkPayload))
      .build()

    val response = grpcClient.messageSendUrl(request)
    response.getId.getValue
  }

  override def messageRecall(messageId: String): Boolean = {
    val request = Message.MessageRecallRequest.newBuilder()
      .setId(messageId)
      .build()

    val response = grpcClient.messageRecall(request)
    response.getSuccess
  }

  override def messageSendText(conversationID: String, text: String, mentionIDList: String*): String = {
    info("PuppetHostie messageSendText({}, {})", conversationID, text)
    val request = MessageSendTextRequest
      .newBuilder()
      .setConversationId(conversationID)
      .setText(text).build()
    val response = grpcClient.messageSendText(request)
    response.getId.getValue
  }

  override protected def messageRawPayload(id: String): MessagePayload = {
    info("PuppetHostie MessagePayload({})", id)
    val response = grpcClient.messagePayload(MessagePayloadRequest.newBuilder().setId(id).build())
    val messagePayload = new MessagePayload
    messagePayload.id = response.getId
    messagePayload.mentionIdList = response.getMentionIdsList.toArray(Array[String]())
    messagePayload.filename = response.getFilename
    messagePayload.text = response.getText
    messagePayload.timestamp = response.getTimestamp
    println("respon.getTypeValue",response.getTypeValue,response.getType)
    if (response.getTypeValue > MessageType.Video.id)
      messagePayload.`type` = MessageType.Unknown
    else
      messagePayload.`type` = MessageType.apply(response.getTypeValue)

    messagePayload.fromId = response.getFromId
    messagePayload.roomId = response.getRoomId
    messagePayload.toId = response.getToId

    messagePayload
  }

  override protected def ding(data: String): Unit = {
    val request = DingRequest.newBuilder().setData(data).build()
    grpcClient.ding(request)
  }
}
