package wechaty.padplus.support

import wechaty.padplus.grpc.PadPlusServerOuterClass.ApiType
import wechaty.puppet.ResourceBox
import wechaty.puppet.schemas.{Contact, Puppet}
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
  override protected def contactRawPayload(contactId: String): Contact.ContactPayload = {
    val json = Puppet.objectMapper.createObjectNode()
    json.put("userName",contactId)
    val response = request[](ApiType.GET_CONTACT,Some(json.toString))
  }
}
