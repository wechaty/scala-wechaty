package wechaty.puppet.support

import com.github.benmanes.caffeine.cache.Cache
import com.typesafe.scalalogging.LazyLogging
import wechaty.puppet.schemas.Image.ImageType
import wechaty.puppet.schemas.Message.{MessagePayload, MessageType}
import wechaty.puppet.schemas.MiniProgram.MiniProgramPayload
import wechaty.puppet.schemas.Puppet
import wechaty.puppet.schemas.UrlLink.UrlLinkPayload
import wechaty.puppet.{Puppet, ResourceBox}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-03
  */
trait MessageSupport {
  self: LazyLogging with Puppet =>
  private[puppet] val cacheMessagePayload = createCache().asInstanceOf[Cache[String, MessagePayload]]

  /**
    * message
    */
  def messageContact(messageId: String): String

  def messageFile(messageId: String):ResourceBox
  def messageImage(messageId: String, imageType: ImageType.Type) : ResourceBox
  def messageMiniProgram(messageId: String): MiniProgramPayload

  def messageUrl(messageId: String): UrlLinkPayload

  def messageSendContact(conversationId: String, contactId: String): String

  def messageSendFile         (conversationId: String, file: ResourceBox): String

  def messageSendMiniProgram(conversationId: String, miniProgramPayload: MiniProgramPayload): String

  def messageSendText(conversationId: String, text: String, mentionIdList: Array[String]): String

  def messageSendUrl(conversationId: String, urlLinkPayload: UrlLinkPayload): String

  def messageRecall(messageId: String): Boolean

  def messageSendText(conversationID: String, text: String, mentionIDList: String*): String

  def messagePayload(messageId: String): MessagePayload = {
    logger.debug("Puppet messagePayload({})", messageId)
    if (Puppet.isBlank(messageId)) {
      throw new IllegalArgumentException("message id is blank!")
    }

    /**
      * 1. Try to get from cache first
      */
    val cachedPayload = this.cacheMessagePayload.getIfPresent(messageId)
    if (cachedPayload != null) {
      return cachedPayload
    }

    /**
      * 2. Cache not found
      */
    val payload = messageRawPayload(messageId)

    this.cacheMessagePayload.put(messageId, payload)
    logger.info("Puppet messagePayload({}) cache SET", messageId)

    payload
  }
  def messageForward (conversationId: String, messageId: String): String= {
    val payload = this.messagePayload(messageId)
    payload.`type` match{
      case MessageType.Attachment |  MessageType.Audio  | MessageType.Video =>
        messageSendFile(conversationId,messageFile(messageId))
      case MessageType.Image =>
        messageSendFile(conversationId,messageFile(messageId))
//        messageSendFile(conversationId,messageImage(messageId,ImageType.Thumbnail))
      case MessageType.Text =>
        if(!Puppet.isBlank(payload.text)){
          this.messageSendText(
            conversationId,
            payload.text,
          )
        }
        else {
          throw new IllegalStateException("Puppet messageForward() payload.text is undefined.")
        }
      case MessageType.MiniProgram =>
        this.messageSendMiniProgram(
          conversationId,
          this.messageMiniProgram(messageId) )

      case MessageType.Url =>
        this.messageSendUrl(
          conversationId,
          this.messageUrl(messageId)
        )

      case MessageType.Contact =>
        this.messageSendContact(
          conversationId,
          this.messageContact(messageId)
        )

      case MessageType.ChatHistory | MessageType.Location
           | MessageType.Emoticon | MessageType.Transfer
           | MessageType.RedEnvelope| MessageType.Recalled =>
        throw new UnsupportedOperationException
      case MessageType.Unknown =>
        throw new Error("Unsupported forward message type:" + payload.`type`)
    }

  }

  protected def messageRawPayload(messageId: String): MessagePayload

  protected def ding(data: String): Unit


}
