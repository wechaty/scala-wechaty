package wechaty.padplus.support

import com.typesafe.scalalogging.LazyLogging
import wechaty.padplus.grpc.PadPlusServerOuterClass.ApiType
import wechaty.padplus.schemas.PadplusEnums.PadplusMessageType
import wechaty.puppet.ResourceBox
import wechaty.puppet.schemas.Image.ImageType.Type
import wechaty.puppet.schemas.Message.{MessagePayload, MessageType}
import wechaty.puppet.schemas.{Message, MiniProgram, Puppet, UrlLink}
import wechaty.puppet.support.MessageSupport

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-22
  */
trait MessageRawSupport {
  self:GrpcSupport with GrpcEventSupport with PadplusHelper with LazyLogging with MessageSupport with LocalStoreSupport =>
  /**
    * message
    */
  override def messageContact(messageId: String): String = ???

  override def messageFile(messageId: String): ResourceBox = ???

  override def messageImage(messageId: String, imageType: Type): ResourceBox = ???

  override def messageMiniProgram(messageId: String): MiniProgram.MiniProgramPayload = ???

  override def messageUrl(messageId: String): UrlLink.UrlLinkPayload = ???

  override def messageSendContact(conversationId: String, contactId: String): String = ???

  override def messageSendFile(conversationId: String, file: ResourceBox): String = ???

  override def messageSendMiniProgram(conversationId: String, miniProgramPayload: MiniProgram.MiniProgramPayload): String = ???

  override def messageSendText(conversationId: String, text: String, mentionIdList: Array[String]): String = {
    val json = Puppet.objectMapper.createObjectNode()
    json.put("content",text)
    json.put("messageType",PadplusMessageType.Text.id)
    json.put("fromUserName",selfId.get)
    json.put("toUserName",conversationId)
    val response = request(ApiType.SEND_MESSAGE, Some(json.toString))
    //TODO
    ""
  }

  override def messageSendUrl(conversationId: String, urlLinkPayload: UrlLink.UrlLinkPayload): String = ???

  override def messageRecall(messageId: String): Boolean = ???

  override def messageSendText(conversationID: String, text: String, mentionIDList: String*): String = {
    messageSendText(conversationID,text,mentionIDList.toArray)
  }

  override protected def messageRawPayload(messageId: String): Message.MessagePayload = {
    getGrpcMessagePayload(messageId) match {
      case Some(rawPayload) =>
        val messagePayload = new MessagePayload
        messagePayload.id = rawPayload.MsgId
        messagePayload.`type` = messageType(PadplusMessageType(rawPayload.MsgType))

        /**
          * 1. Set Room Id
          */
        if (isRoomId(rawPayload.FromUserName)) {
          messagePayload.roomId = rawPayload.FromUserName
        } else if (isRoomId(rawPayload.ToUserName)) {
          messagePayload.roomId = rawPayload.ToUserName
        }

        /**
          * 2. Set To Contact Id
          */
        if (isContactId(rawPayload.ToUserName)) {

          messagePayload.toId = rawPayload.ToUserName
        }

        /**
          * 3. Set From Contact Id
          */
        if (isContactId(rawPayload.FromUserName)) {

          messagePayload.fromId = rawPayload.FromUserName

        } else {
          val parts = rawPayload.Content.split(":\n")
          if (parts.length > 1) {
            if (isContactId(parts(0))) {
              messagePayload.fromId = parts(0)
            }
          }
        }

        /**
          *
          * 4. Set Text
          */
        if (isRoomId(rawPayload.FromUserName)) {

          val startIndex = rawPayload.Content.indexOf(":\n")
          messagePayload.text = rawPayload.Content.substring(if (startIndex != -1) startIndex + 2 else 0)

        } else {
          messagePayload.text = rawPayload.Content
        }

        if (messagePayload.`type` == MessageType.Recalled) {
          //not supported
        }


        /**
          * 6. Set mention list, only for room messages
          */
        //TODO
        //    if (roomId) {
        //    const messageSource = await messageSourceParser(rawPayload.msgSource)
        //    if (messageSource !== null && messageSource.atUserList) {
        //    mentionIdList = messageSource.atUserList || []
        //    }
        //    }

        /**
          * 6. Set Contact for ShareCard
          */
        /* if (type === MessageType.Contact) {
        const xml = await xmlToJson(rawPayload.content.split('\n')[1])
        log.silly(PRE, `xml : ${JSON.stringify(xml)}`)
        const shareCardData = xml.msg.$
        text = JSON.stringify(shareCardData)
      } */

        messagePayload
      case _ =>
        throw new IllegalAccessException("message not found by " + messageId)
    }
  }

  override protected def ding(data: String): Unit = ???
}
