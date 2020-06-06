package wechaty.user

import wechaty.Wechaty.PuppetResolver
import wechaty.puppet.LoggerSupport
import wechaty.puppet.schemas.Contact.{ContactGender, ContactPayload, ContactType}
import wechaty.puppet.schemas.Puppet

import scala.language.implicitConversions

/**
  *
  * All wechat contacts(friend) will be encapsulated as a Contact.
  * [Examples/Contact-Bot]{@link https://github.com/wechaty/wechaty/blob/1523c5e02be46ebe2cc172a744b2fbe53351540e/examples/contact-bot.ts}
  *
  * @property {string}  id               - Get Contact id.
  *           This function is depending on the Puppet Implementation, see [puppet-compatible-table](https://github.com/wechaty/wechaty/wiki/Puppet#3-puppet-compatible-table)
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-03
  */
class Contact(contactId: String)(implicit resolver: PuppetResolver) extends LoggerSupport {
  //  lazy val payload: schemas.Contact.ContactPayload = resolver.puppet.contactPayload(contactId)
  def payload: ContactPayload = {
    resolver.puppet.contactPayload(contactId)
  }

  //delegate method
  def id = contactId

  private implicit def toMessage(messageId: String) = new Message(messageId)

  def say(something: String): Message = {
    resolver.puppet.messageSendText(this.id, something)
  }

  def say(something: Contact): Message = {
    resolver.puppet.messageSendContact(this.id, something.id)
  }

  def say(something: UrlLink): Message = {
    resolver.puppet.messageSendUrl(this.id, something.payload)
  }

  def say(something: MiniProgram): Message = {
    resolver.puppet.messageSendMiniProgram(this.id, something.payload)
  }

  /**
    * Get the name from a contact
    *
    * @returns {string}
    * @example
    * const name = contact.name()
    */
  def name(): String = {
    if (this.payload != null)
      this.payload.name
    else null
  }


  /**
    * GET / SET / DELETE the alias for a contact
    *
    * Tests show it will failed if set alias too frequently(60 times in one minute).
    *
    * @param {(none | string | null)} newAlias
    * @returns {(Promise<null | string | void>)}
    * @example <caption> GET the alias for a contact, return {(Promise<string | null>)}</caption>
    *          const alias = await contact.alias()
    *          if (alias === null) {
    *   console.log('You have not yet set any alias for contact ' + contact.name())
    *          } else {
    *   console.log('You have already set an alias for contact ' + contact.name() + ':' + alias)
    *          }
    * @example <caption>SET the alias for a contact</caption>
    *          try {
    *          await contact.alias('lijiarui')
    *   console.log(`change ${contact.name()}'s alias successfully!`)
    *          } catch (e) {
    *   console.log(`failed to change ${contact.name()} alias!`)
    *          }
    * @example <caption>DELETE the alias for a contact</caption>
    *          try {
    *          const oldAlias = await contact.alias(null)
    *   console.log(`delete ${contact.name()}'s alias successfully!`)
    *   console.log('old alias is ${oldAlias}`)
    *          } catch (e) {
    *   console.log(`failed to delete ${contact.name()}'s alias!`)
    *          }
    **/
  def alias(newAlias: String): String = {

    if (this.payload == null) {
      throw new Error("no payload")
    }


    resolver.puppet.contactAlias(this.id, newAlias)
    resolver.puppet.contactPayloadDirty(this.id)
    //TODO refresh this.payload object
    this.payload.alias
  }

  /**
    *
    * @description
    * Should use { @link Contact#friend} instead
    * @deprecated
    * @ignore
    */
  def stranger(): Boolean = {
    if (this.payload == null) return false
    else !this.friend()
  }

  /**
    * Check if contact is friend
    *
    * > Tips:
    * This function is depending on the Puppet Implementation, see [puppet-compatible-table](https://github.com/wechaty/wechaty/wiki/Puppet#3-puppet-compatible-table)
    *
    * @returns {boolean | null}
    *
    *          <br>True for friend of the bot <br>
    *          False for not friend of the bot, null for unknown.
    * @example
    * const isFriend = contact.friend()
    */
  def friend(): Boolean = {
    if (this.payload == null) false
    else this.payload.friend
  }


  /**
    * Enum for ContactType
    *
    * @enum {number}
    * @property {number} Unknown    - ContactType.Unknown    (0) for Unknown
    * @property {number} Personal   - ContactType.Personal   (1) for Personal
    * @property {number} Official   - ContactType.Official   (2) for Official
    */

  /**
    * Return the type of the Contact
    * > Tips: ContactType is enum here.</br>
    *
    * @returns {ContactType.Unknown | ContactType.Personal | ContactType.Official}
    * @example
    * const bot = new Wechaty()
    * await bot.start()
    * const isOfficial = contact.type() === bot.Contact.Type.Official
    */
  def `type`(): ContactType.Type = {
    if (this.payload == null) {
      throw new Error("no payload")
    }
    this.payload.`type`
  }

  /**
    * @ignore
    * TODO
    * Check if the contact is star contact.
    * @returns {boolean | null} - True for star friend, False for no star friend.
    * @example
    * const isStar = contact.star()
    */
  def star(): Boolean = {
    if (this.payload == null) false
    else this.payload.star
  }

  /**
    * Contact gender
    * > Tips: ContactGender is enum here. </br>
    *
    * @returns {ContactGender.Unknown | ContactGender.Male | ContactGender.Female}
    * @example
    * const gender = contact.gender() === bot.Contact.Gender.Male
    */
  def gender(): ContactGender.Type = {
    if (this.payload == null) ContactGender.Unknown
    else payload.gender
  }

  /**
    * Get the region 'province' from a contact
    *
    * @returns {string | null}
    * @example
    * const province = contact.province()
    */
  def province(): String = {
    if (this.payload == null) null
    else this.payload.province
  }

  /**
    * Get the region 'city' from a contact
    *
    * @returns {string | null}
    * @example
    * const city = contact.city()
    */
  def city(): String = {
    if (this.payload == null) null
    else this.payload.city
  }


  /**
    * Get all tags of contact
    *
    * @returns {Promise<Tag[]>}
    * @example
    * const tags = await contact.tags()
    */
  def tags(): Array[Tag] = {

    val tagIdList = resolver.puppet.tagContactList(this.id)
    tagIdList.map(id => new Tag(id))
  }


  /**
    * Force reload data for Contact, Sync data from lowlevel API again.
    *
    * @returns {Promise<this>}
    * @example
    * await contact.sync()
    */
  def sync(): Unit = {
    this.ready(true)
  }

  /**
    * `ready()` is For FrameWork ONLY!
    *
    * Please not to use `ready()` at the user land.
    * If you want to sync data, uyse `sync()` instead.
    *
    * @ignore
    */
  def ready(forceSync: Boolean = false): Unit = {

    if (!forceSync && this.isReady()) { // already ready
      debug("Contact ready() isReady() true")
      return
    }

    if (forceSync) {
      resolver.puppet.contactPayloadDirty(this.id)
    }
    //TODO reload payload
    //    this.payload = resolver.puppet.contactPayload(this.id)
  }

  /**
    * @ignore
    */
  def isReady(): Boolean = {
    this.payload != null && !Puppet.isBlank(this.payload.name)
  }

  /**
    * Check if contact is self
    *
    * @returns {boolean} True for contact is self, False for contact is others
    * @example
    * const isSelf = contact.self()
    */
  def self(): Boolean = {
    val userIdOpt = resolver.puppet.selfIdOpt()
    userIdOpt match {
      case Some(userId) =>
        this.id == userId
      case _ => false
    }
  }

  /**
    * Get the weixin number from a contact.
    *
    * Sometimes cannot get weixin number due to weixin security mechanism, not recommend.
    *
    * @ignore
    * @returns {string | null}
    * @example
    * const weixin = contact.weixin()
    */
  def weixin(): String = {
    if (this.payload == null) null
    else payload.weixin
  }

  override def toString: String = {

    val identity =
      if (!Puppet.isBlank(payload.alias))
        this.payload.alias
      else if (!Puppet.isBlank(payload.name))
        payload.name
      else if (!Puppet.isBlank(this.id))
        id
      else "loading..."

    s"Contact<${identity}>"
  }
}
