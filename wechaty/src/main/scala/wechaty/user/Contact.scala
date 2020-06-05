package wechaty.user

import wechaty.Wechaty.PuppetResolver
import wechaty.puppet.schemas
import wechaty.puppet.schemas.Puppet

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-03
  */
class Contact(contactId:String)(implicit resolver:PuppetResolver) {
  lazy val payload: schemas.Contact.ContactPayload = resolver.puppet.contactPayload(contactId)
  //delegate method
  def id=contactId
  def name = payload.name

  def say(something:  String): String ={
    resolver.puppet.messageSendText(this.id,something)
  }
  /*
  def say(something:  Contact): String ={
    resolver.puppet.messageSendText(this.id,something)
    resolver.puppet.messageSendText(this.id,something)
  }
    } else if (something instanceof Contact) {
      /**
        * 2. Contact
        */
      msgId = await this.puppet.messageSendContact(
        this.id,
        something.id,
      )
    } else if (something instanceof FileBox) {
      /**
        * 3. File
        */
      msgId = await this.puppet.messageSendFile(
        this.id,
        something,
      )
    } else if (something instanceof UrlLink) {
      /**
        * 4. Link Message
        */
      msgId = await this.puppet.messageSendUrl(
        this.id,
        something.payload,
      )
    } else if (something instanceof MiniProgram) {
      /**
        * 5. Mini Program
        */
      msgId = await this.puppet.messageSendMiniProgram(
        this.id,
        something.payload,
      )
    } else {
      throw new Error('unsupported arg: ' + something)
    }
    if (msgId) {
      const msg = this.wechaty.Message.load(msgId)
      await msg.ready()
      return msg
    }
  }

  /**
    * Get the name from a contact
    *
    * @returns {string}
    * @example
    * const name = contact.name()
    */
  public name (): string {
    return (this.payload && this.payload.name) || ''
  }

  public async alias ()                  : Promise<null | string>
    public async alias (newAlias:  string) : Promise<void>
    public async alias (empty:     null)   : Promise<void>

    /**
      * GET / SET / DELETE the alias for a contact
      *
      * Tests show it will failed if set alias too frequently(60 times in one minute).
      * @param {(none | string | null)} newAlias
      * @returns {(Promise<null | string | void>)}
      * @example <caption> GET the alias for a contact, return {(Promise<string | null>)}</caption>
      * const alias = await contact.alias()
      * if (alias === null) {
      *   console.log('You have not yet set any alias for contact ' + contact.name())
      * } else {
      *   console.log('You have already set an alias for contact ' + contact.name() + ':' + alias)
      * }
      *
      * @example <caption>SET the alias for a contact</caption>
      * try {
      *   await contact.alias('lijiarui')
      *   console.log(`change ${contact.name()}'s alias successfully!`)
      * } catch (e) {
      *   console.log(`failed to change ${contact.name()} alias!`)
      * }
      *
      * @example <caption>DELETE the alias for a contact</caption>
      * try {
      *   const oldAlias = await contact.alias(null)
      *   console.log(`delete ${contact.name()}'s alias successfully!`)
      *   console.log('old alias is ${oldAlias}`)
      * } catch (e) {
      *   console.log(`failed to delete ${contact.name()}'s alias!`)
      * }
      */
    public async alias (newAlias?: null | string): Promise<null | string | void> {
    log.silly('Contact', 'alias(%s)',
    newAlias === undefined
    ? ''
    : newAlias,
    )

    if (!this.payload) {
      throw new Error('no payload')
    }

    if (typeof newAlias === 'undefined') {
      return this.payload.alias || null
    }

    try {
      await this.puppet.contactAlias(this.id, newAlias)
      await this.puppet.contactPayloadDirty(this.id)
      this.payload = await this.puppet.contactPayload(this.id)
      if (newAlias && newAlias !== this.payload.alias) {
        log.warn('Contact', 'alias(%s) sync with server fail: set(%s) is not equal to get(%s)',
        newAlias,
        newAlias,
        this.payload.alias,
        )
      }
    } catch (e) {
      log.error('Contact', 'alias(%s) rejected: %s', newAlias, e.message)
      Raven.captureException(e)
    }
  }

  /**
    *
    * @description
    * Should use {@link Contact#friend} instead
    *
    * @deprecated
    * @ignore
    */
  public stranger (): null | boolean {
    log.warn('Contact', 'stranger() DEPRECATED. use friend() instead.')
    if (!this.payload) return null
    return !this.friend()
  }

  /**
    * Check if contact is friend
    *
    * > Tips:
    * This function is depending on the Puppet Implementation, see [puppet-compatible-table](https://github.com/wechaty/wechaty/wiki/Puppet#3-puppet-compatible-table)
    *
    * @returns {boolean | null}
    *
    * <br>True for friend of the bot <br>
    * False for not friend of the bot, null for unknown.
    * @example
    * const isFriend = contact.friend()
    */
  public friend (): null | boolean {
    log.verbose('Contact', 'friend()')
    if (!this.payload) {
    return null
  }
    return this.payload.friend || null
  }

  /**
    * @ignore
    * @see {@link https://github.com/Chatie/webwx-app-tracker/blob/7c59d35c6ea0cff38426a4c5c912a086c4c512b2/formatted/webwxApp.js#L3243|webwxApp.js#L324}
    * @see {@link https://github.com/Urinx/WeixinBot/blob/master/README.md|Urinx/WeixinBot/README}
    */
  /**
    * @description
    * Check if it's a offical account, should use {@link Contact#type} instead
    * @deprecated
    * @ignore
    */
  public official (): boolean {
    log.warn('Contact', 'official() DEPRECATED. use type() instead')
    return !!this.payload && (this.payload.type === ContactType.Official)
  }

  /**
    * @description
    * Check if it's a personal account, should use {@link Contact#type} instead
    * @deprecated
    * @ignore
    */
  public personal (): boolean {
    log.warn('Contact', 'personal() DEPRECATED. use type() instead')
    return !!this.payload && this.payload.type === ContactType.Personal
  }

  /**
    * Enum for ContactType
    * @enum {number}
    * @property {number} Unknown    - ContactType.Unknown    (0) for Unknown
    * @property {number} Personal   - ContactType.Personal   (1) for Personal
    * @property {number} Official   - ContactType.Official   (2) for Official
    */

  /**
    * Return the type of the Contact
    * > Tips: ContactType is enum here.</br>
    * @returns {ContactType.Unknown | ContactType.Personal | ContactType.Official}
    *
    * @example
    * const bot = new Wechaty()
    * await bot.start()
    * const isOfficial = contact.type() === bot.Contact.Type.Official
    */
  public type (): ContactType {
    if (!this.payload) {
    throw new Error('no payload')
  }
    return this.payload.type
  }

  /**
    * @ignore
    * TODO
    * Check if the contact is star contact.
    *
    * @returns {boolean | null} - True for star friend, False for no star friend.
    * @example
    * const isStar = contact.star()
    */
  public star (): null | boolean {
    if (!this.payload) {
    return null
  }
    return this.payload.star === undefined
    ? null
    : this.payload.star
  }

  /**
    * Contact gender
    * > Tips: ContactGender is enum here. </br>
    *
    * @returns {ContactGender.Unknown | ContactGender.Male | ContactGender.Female}
    * @example
    * const gender = contact.gender() === bot.Contact.Gender.Male
    */
  public gender (): ContactGender {
    return this.payload
    ? this.payload.gender
    : ContactGender.Unknown
  }

  /**
    * Get the region 'province' from a contact
    *
    * @returns {string | null}
    * @example
    * const province = contact.province()
    */
  public province (): null | string {
    return (this.payload && this.payload.province) || null
  }

  /**
    * Get the region 'city' from a contact
    *
    * @returns {string | null}
    * @example
    * const city = contact.city()
    */
  public city (): null | string {
    return (this.payload && this.payload.city) || null
  }

  /**
    * Get avatar picture file stream
    *
    * @returns {Promise<FileBox>}
    * @example
    * // Save avatar to local file like `1-name.jpg`
    *
    * const file = await contact.avatar()
    * const name = file.name
    * await file.toFile(name, true)
    * console.log(`Contact: ${contact.name()} with avatar file: ${name}`)
    */
  public async avatar (): Promise<FileBox> {
    log.verbose('Contact', 'avatar()')

    try {
    const fileBox = await this.puppet.contactAvatar(this.id)
    return fileBox
  } catch (e) {
    log.error('Contact', 'avatar() exception: %s', e.message)
    return qrCodeForChatie()
  }
  }

  /**
    * Get all tags of contact
    *
    * @returns {Promise<Tag[]>}
    * @example
    * const tags = await contact.tags()
    */
  public async tags (): Promise<Tag []> {
    log.verbose('Contact', 'tags() for %s', this)

    try {
    const tagIdList = await this.puppet.tagContactList(this.id)
    const tagList = tagIdList.map(id => this.wechaty.Tag.load(id))
    return tagList
  } catch (e) {
    log.error('Contact', 'tags() exception: %s', e.message)
    return []
  }
  }

  /**
    * @description
    * Force reload(re-ready()) data for Contact, use {@link Contact#sync} instead
    *
    * @deprecated
    * @ignore
    */
  public refresh (): Promise<void> {
    log.warn('Contact', 'refresh() DEPRECATED. use sync() instead.')
    return this.sync()
  }

  /**
    * Force reload data for Contact, Sync data from lowlevel API again.
    *
    * @returns {Promise<this>}
    * @example
    * await contact.sync()
    */
  public async sync (): Promise<void> {
    await this.ready(true)
  }

  /**
    * `ready()` is For FrameWork ONLY!
    *
    * Please not to use `ready()` at the user land.
    * If you want to sync data, uyse `sync()` instead.
    *
    * @ignore
    */
  public async ready (
    forceSync = false,
  ): Promise<void> {
    log.silly('Contact', 'ready() @ %s with id="%s"', this.puppet, this.id)

    if (!forceSync && this.isReady()) { // already ready
    log.silly('Contact', 'ready() isReady() true')
    return
  }

    try {
    if (forceSync) {
    await this.puppet.contactPayloadDirty(this.id)
  }
    this.payload = await this.puppet.contactPayload(this.id)
    // log.silly('Contact', `ready() this.puppet.contactPayload(%s) resolved`, this)

  } catch (e) {
    log.verbose('Contact', 'ready() this.puppet.contactPayload(%s) exception: %s',
    this.id,
    e.message,
    )
    Raven.captureException(e)
    throw e
  }
  }

  /**
    * @ignore
    */
  public isReady (): boolean {
    return !!(this.payload && this.payload.name)
  }

  /**
    * Check if contact is self
    *
    * @returns {boolean} True for contact is self, False for contact is others
    * @example
    * const isSelf = contact.self()
    */
  public self (): boolean {
    const userId = this.puppet.selfId()

    if (!userId) {
    return false
  }

    return this.id === userId
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
  public weixin (): null | string {
    return (this.payload && this.payload.weixin) || null
  }
  */

  override def toString: String = {

    val identity =
      if(!Puppet.isBlank(payload.alias))
        this.payload.alias
      else if(!Puppet.isBlank(payload.name))
        payload.name
      else if(!Puppet.isBlank(this.id))
        id
      else "loading..."

    s"Contact<${identity}>"
  }
}
