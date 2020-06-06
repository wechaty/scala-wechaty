package wechaty.puppet.support

import com.github.benmanes.caffeine.cache.Cache
import wechaty.puppet.{LoggerSupport, Puppet}
import wechaty.puppet.schemas.Contact.ContactPayload
import wechaty.puppet.schemas.Puppet

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-03
  */
trait ContactSupport {
  self:Puppet with LoggerSupport =>
  private val cacheContactPayload = createCache().asInstanceOf[Cache[String,ContactPayload]]
  /**
    *
    * Contact
    *
    */
 def contactAlias (contactId: String)                       : String
   def contactAlias (contactId: String, alias: String) : Unit

//   def contactAvatar (contactId: String)                : FileBox>
//   def contactAvatar (contactId: String, file: FileBox) : Promise<void>

   def contactList ()                   : Array[String]

  /**
    * contact
    */
  protected def contactRawPayload(contactId: String): ContactPayload
  def contactPayload(contactId:String):ContactPayload={
    if (Puppet.isBlank(contactId)) {
      throw new IllegalArgumentException("contact id is blank!")
    }

    /**
      * 1. Try to get from cache first
      */
    val cachedPayload = this.cacheContactPayload.getIfPresent(contactId)
    if (cachedPayload !=null) {
      return cachedPayload
    }

    /**
      * 2. Cache not found
      */
    val payload = this.contactRawPayload(contactId)

    cacheContactPayload.put(contactId, payload)
    info("Puppet contactPayload({}) cache SET", contactId)

    payload
  }

}
