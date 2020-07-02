package wechaty.puppet.support

import com.github.benmanes.caffeine.cache.Cache
import com.typesafe.scalalogging.LazyLogging
import wechaty.puppet.ResourceBox
import wechaty.puppet.schemas.Contact.ContactPayload
import wechaty.puppet.schemas.Puppet
import wechaty.puppet.schemas.Puppet.executionContext

import scala.concurrent.Future

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-03
  */
trait ContactSupport {
  self: CacheSupport with LazyLogging =>
  private[puppet] val cacheContactPayload = createCache().asInstanceOf[Cache[String, ContactPayload]]

  /**
    *
    * Contact
    *
    */
  def contactAlias(contactId: String): String

  def contactAlias(contactId: String, alias: String): Unit

  def contactAvatar (contactId: String)                : ResourceBox
  def contactAvatar (contactId: String, file: ResourceBox) : ResourceBox

  def contactList(): Array[String]

  def contactPayload(contactId: String): Future[ContactPayload] = {
    if (Puppet.isBlank(contactId)) {
      throw new IllegalArgumentException("contact id is blank!")
    }

    /**
      * 1. Try to get from cache first
      */
    val cachedPayload = this.cacheContactPayload.getIfPresent(contactId)
    if (cachedPayload != null) {
      return Future.successful(cachedPayload)
    }

    /**
      * 2. Cache not found
      */
    val payloadFuture = this.contactRawPayload(contactId)

    payloadFuture.map(payload=>{
      cacheContactPayload.put(contactId, payload)
      logger.info("Puppet contactPayload({}) cache SET", contactId)
      payload
    })
  }
  def contactPayloadDirty (contactId: String): Unit ={
    logger.debug("Puppet contactPayloadDirty({})", contactId)
    this.cacheContactPayload.invalidate(contactId)
  }

  /**
    * contact
    */
  protected def contactRawPayload(contactId: String): Future[ContactPayload]

}
