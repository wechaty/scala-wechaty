package wechaty.hostie

import io.github.wechaty.grpc.puppet.Contact.ContactPayloadRequest
import wechaty.puppet.schemas.Contact
import wechaty.puppet.schemas.Contact.ContactPayload
import wechaty.puppet.schemas.Events.EventLoginPayload

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
trait ContactSupport {
  self: GrpcSupport =>
  def contactRawPayload(contactID: String): ContactPayload= {
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
    contact.weiXin = response.getWeixin

    contact
  }
  def toContactPayload(eventLoginPayload: EventLoginPayload):ContactPayload={
    contactRawPayload(eventLoginPayload.contactId)
  }
}
