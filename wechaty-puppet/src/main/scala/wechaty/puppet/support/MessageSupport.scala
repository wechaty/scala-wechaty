package wechaty.puppet.support

import com.github.benmanes.caffeine.cache.Cache
import wechaty.puppet.schemas.Message.MessagePayload
import wechaty.puppet.schemas.MiniProgram.MiniProgramPayload
import wechaty.puppet.schemas.Puppet
import wechaty.puppet.schemas.UrlLink.UrlLinkPayload
import wechaty.puppet.{LoggerSupport, Puppet}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-03
  */
trait MessageSupport {
  self: LoggerSupport with Puppet =>
  private val cacheMessagePayload = createCache().asInstanceOf[Cache[String, MessagePayload]]

  /**
    * message
    */
  def messageContact(messageId: String): String

  //  def messageFile(messageId: String)FileBox
  //  def messageImage(messageId: String, imageType: ImageType.Type) : FileBox
  def messageMiniProgram(messageId: String): MiniProgramPayload

  def messageUrl(messageId: String): UrlLinkPayload

  def messageSendContact(conversationId: String, contactId: String): String

  //   def messageSendFile         (conversationId: String, file: FileBox)                          : Promise<void | String>
  def messageSendMiniProgram(conversationId: String, miniProgramPayload: MiniProgramPayload): String

  def messageSendText(conversationId: String, text: String, mentionIdList: Array[String]): String

  def messageSendUrl(conversationId: String, urlLinkPayload: UrlLinkPayload): String

  def messageRecall(messageId: String): Boolean

  def messageSendText(conversationID: String, text: String, mentionIDList: String*): String

  def messagePayload(messageId: String): MessagePayload = {
    debug("Puppet messagePayload({})", messageId)
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
    info("Puppet messagePayload({}) cache SET", messageId)

    payload
  }

  protected def messageRawPayload(messageId: String): MessagePayload

  protected def ding(data: String): Unit


}
