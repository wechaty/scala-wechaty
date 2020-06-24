package wechaty.padplus.support

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.typesafe.scalalogging.LazyLogging
import wechaty.padplus.grpc.PadPlusServerOuterClass.{ApiType, ResponseType, StreamResponse}
import wechaty.padplus.schemas.GrpcSchemas.GrpcMessagePayload
import wechaty.padplus.schemas.ModelMessage.PadplusMessagePayload
import wechaty.padplus.schemas.PadplusEnums.PadplusMessageType
import wechaty.puppet.ResourceBox
import wechaty.puppet.events.EventEmitter
import wechaty.puppet.schemas.Event.EventMessagePayload
import wechaty.puppet.schemas.Image.ImageType.Type
import wechaty.puppet.schemas.Message.{MessagePayload, MessageType}
import wechaty.puppet.schemas.Puppet.{PuppetEventName, isBlank, objectMapper}
import wechaty.puppet.schemas.{Message, MiniProgram, Puppet, UrlLink}
import wechaty.puppet.support.MessageSupport

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-22
  */
trait MessageRawSupport {
  self:GrpcSupport with EventEmitter with GrpcEventSupport with PadplusHelper with LazyLogging with MessageSupport with LocalStoreSupport =>
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


  override protected def messageRawPayload(messageId: String): Message.MessagePayload = {
    getPadplusMessagePayload(messageId) match {
      case Some(rawPayload) =>
        val messagePayload = new MessagePayload
        messagePayload.id = rawPayload.msgId
        messagePayload.`type` = messageType(rawPayload.msgType)

        /**
          * 1. Set Room Id
          */
        if (isRoomId(rawPayload.fromUserName)) {
          messagePayload.roomId = rawPayload.fromUserName
        } else if (isRoomId(rawPayload.toUserName)) {
          messagePayload.roomId = rawPayload.toUserName
        }

        /**
          * 2. Set To Contact Id
          */
        if (isContactId(rawPayload.toUserName)) {

          messagePayload.toId = rawPayload.toUserName
        }

        /**
          * 3. Set From Contact Id
          */
        if (isContactId(rawPayload.fromUserName)) {

          messagePayload.fromId = rawPayload.fromUserName

        } else {
          val parts = rawPayload.content.split(":\n")
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
        if (isRoomId(rawPayload.fromUserName)) {

          val startIndex = rawPayload.content.indexOf(":\n")
          messagePayload.text = rawPayload.content.substring(if (startIndex != -1) startIndex + 2 else 0)

        } else {
          messagePayload.text = rawPayload.content
        }

        if (messagePayload.`type` == MessageType.Recalled) {
          //not supported
        }


        /**
          * 6. Set mention list, only for room messages
          */
        if (!isBlank(messagePayload.roomId)) {
          val xmlMapper = new XmlMapper();
          println(rawPayload.msgSource)
          val root = xmlMapper.readTree(rawPayload.msgSource)
          if(root.has("atuserlist")){
            messagePayload.mentionIdList = root.get("atuserlist").asText().split(",")
          }
        }

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
  def messagePartialFunction(response:StreamResponse):PartialFunction[ResponseType,Unit] = {
    case ResponseType.MESSAGE_RECEIVE =>
      val rawMessageStr                            = response.getData()
      val payload                                  = objectMapper.readValue(rawMessageStr, classOf[GrpcMessagePayload])
      val eventMessagePayload                      = new EventMessagePayload
      eventMessagePayload.messageId = payload.MsgId
      savePadplusMessagePayload(payload)
      emit(PuppetEventName.MESSAGE, eventMessagePayload)
  }
  private implicit def convertMessageFromGrpcToPadplus (rawMessage: GrpcMessagePayload): PadplusMessagePayload= {
    val padplusMessagePayload = new  PadplusMessagePayload
    padplusMessagePayload.appMsgType= rawMessage.AppMsgType
    padplusMessagePayload.content= rawMessage.Content
    padplusMessagePayload.createTime= rawMessage.CreateTime
    padplusMessagePayload.fileName= if(isBlank(rawMessage.FileName))  rawMessage.fileName else rawMessage.FileName
    padplusMessagePayload.fromMemberNickName= rawMessage.FromMemberNickName
    padplusMessagePayload.fromMemberUserName= rawMessage.FromMemberUserName
    padplusMessagePayload.fromUserName= rawMessage.FromUserName
    padplusMessagePayload.imgBuf= rawMessage.ImgBuf
    padplusMessagePayload.imgStatus= rawMessage.ImgStatus
    padplusMessagePayload.l1MsgType= rawMessage.L1MsgType
    padplusMessagePayload.msgId= rawMessage.MsgId
    padplusMessagePayload.msgSource= rawMessage.MsgSource
    padplusMessagePayload.msgSourceCd= rawMessage.msgSourceCd
    padplusMessagePayload.msgType= PadplusMessageType(rawMessage.MsgType)
    padplusMessagePayload.newMsgId= rawMessage.NewMsgId
    padplusMessagePayload.pushContent= rawMessage.PushContent
    padplusMessagePayload.status= rawMessage.Status
    padplusMessagePayload.toUserName= rawMessage.ToUserName
    padplusMessagePayload.uin= rawMessage.Uin
    padplusMessagePayload.url= rawMessage.Url
    padplusMessagePayload.wechatUserName= rawMessage.wechatUserName
    padplusMessagePayload
  }
}
