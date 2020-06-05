package wechaty.puppet.support

import com.github.benmanes.caffeine.cache.Cache
import wechaty.puppet.schemas.Message.MessagePayload
import wechaty.puppet.schemas.Puppet
import wechaty.puppet.{LoggerSupport, Puppet}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-03
  */
trait MessageSupport {
  self :LoggerSupport with Puppet =>
  private val cacheMessagePayload = createCache().asInstanceOf[Cache[String,MessagePayload]]
  /**
    * message
    */
  protected def messageRawPayload (messageId: String):MessagePayload
  protected def ding(data:String):Unit
  def messageSendText(conversationID :String , text :String , mentionIDList:String*):String
  def messagePayload(messageId:String):MessagePayload={
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


}
