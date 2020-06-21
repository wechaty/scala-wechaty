package wechaty.padplus.support

import wechaty.puppet.ResourceBox
import wechaty.puppet.schemas.Contact
import wechaty.puppet.support.ContactSupport

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-21
  */
trait ContactRawSupport {
  self: GrpcSupport with ContactSupport =>
  /**
    *
    * Contact
    *
    */
  override def contactAlias(contactId: String): String = ???

  override def contactAlias(contactId: String, alias: String): Unit = ???

  override def contactAvatar(contactId: String): ResourceBox = ???

  override def contactAvatar(contactId: String, file: ResourceBox): ResourceBox = ???

  override def contactList(): Array[String] = ???

  /**
    * contact
    */
  override protected def contactRawPayload(contactId: String): Contact.ContactPayload = ???
}
