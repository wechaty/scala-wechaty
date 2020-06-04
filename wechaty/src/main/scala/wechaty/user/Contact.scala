package wechaty.user

import wechaty.Wechaty.PuppetResolver
import wechaty.puppet.schemas

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
}
