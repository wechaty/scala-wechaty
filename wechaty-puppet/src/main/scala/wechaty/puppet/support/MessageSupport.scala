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
  /*
  def messageContact(messageId: String):String
//  def messageFile(messageId: String)FileBox
//  def messageImage(messageId: String, imageType: ImageType.Type) : FileBox
    public abstract async messageMiniProgram  (messageId: string)                       : Promise<MiniProgramPayload>
    public abstract async messageUrl          (messageId: string)                       : Promise<UrlLinkPayload>

    public abstract async messageSendContact      (conversationId: string, contactId: string)                      : Promise<void | string>
    public abstract async messageSendFile         (conversationId: string, file: FileBox)                          : Promise<void | string>
    public abstract async messageSendMiniProgram  (conversationId: string, miniProgramPayload: MiniProgramPayload) : Promise<void | string>
    public abstract async messageSendText         (conversationId: string, text: string, mentionIdList?: string[]) : Promise<void | string>
    public abstract async messageSendUrl          (conversationId: string, urlLinkPayload: UrlLinkPayload)         : Promise<void | string>

    public abstract async messageRecall (messageId: string) : Promise<boolean>

  protected abstract async messageRawPayload (messageId: string)     : Promise<any>
  protected abstract async messageRawPayloadParser (rawPayload: any) : Promise<MessagePayload>
  */
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
