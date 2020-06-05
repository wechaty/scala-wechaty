package wechaty.hostie.support

import io.github.wechaty.grpc.puppet.Contact.ContactPayloadRequest
import wechaty.puppet.Puppet
import wechaty.puppet.schemas.Contact
import wechaty.puppet.schemas.Contact.ContactPayload

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
trait ContactRawSupport {
  self: GrpcSupport with Puppet =>
  override protected def contactRawPayload(contactID: String): ContactPayload= {
    val response = grpcClient.contactPayload(ContactPayloadRequest.newBuilder().setId(contactID).build())
    val contact = new ContactPayload
    contact.id = response.getId
    contact.gender = Contact.ContactGender.apply(response.getGenderValue)
    contact.`type` = Contact.ContactType.apply(response.getTypeValue)
    contact.name = response.getName
    contact.avatar = response.getAvatar
    contact.address = response.getAddress
    contact.alias = response.getAlias
    contact.city = response.getCity
    contact.friend = response.getFriend
    contact.province = response.getProvince
    contact.signature = response.getSignature
    contact.star = response.getStar
    contact.weixin = response.getWeixin

    contact
  }
}
