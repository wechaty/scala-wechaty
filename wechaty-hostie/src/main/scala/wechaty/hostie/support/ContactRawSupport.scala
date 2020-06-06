package wechaty.hostie.support

import com.google.protobuf.StringValue
import io.github.wechaty.grpc.puppet.Contact
import io.github.wechaty.grpc.puppet.Contact.ContactPayloadRequest
import wechaty.puppet.Puppet
import wechaty.puppet.schemas.Contact.ContactPayload

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
trait ContactRawSupport {
  self: GrpcSupport with Puppet =>

  /**
    *
    * Contact
    *
    */
  override def contactAlias(contactId: String): String = {
    val request = Contact.ContactAliasRequest.newBuilder()
      .setId(contactId)
      .build()
    val response = grpcClient.contactAlias(request)
    response.getAlias.getValue
  }

  override def contactAlias(contactId: String, alias: String): Unit = {
    val stringValue = StringValue.newBuilder().setValue(alias).build()
    val request = Contact.ContactAliasRequest.newBuilder()
      .setId(contactId)
      .setAlias(stringValue)
      .build()
    grpcClient.contactAlias(request)
  }

  override def contactList(): Array[String] = {
    val request = Contact.ContactListRequest.newBuilder().build()

    val response = grpcClient.contactList(request)
    response.getIdsList.toArray(Array[String]())
  }

  override protected def contactRawPayload(contactID: String): ContactPayload = {
    val response = grpcClient.contactPayload(ContactPayloadRequest.newBuilder().setId(contactID).build())
    val contact = new ContactPayload
    contact.id = response.getId
    contact.gender = wechaty.puppet.schemas.Contact.ContactGender.apply(response.getGenderValue)
    contact.`type` = wechaty.puppet.schemas.Contact.ContactType.apply(response.getTypeValue)
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
