package wechaty.hostie.support

import com.google.protobuf.StringValue
import io.github.wechaty.grpc.puppet.Contact
import io.github.wechaty.grpc.puppet.Contact.ContactPayloadRequest
import wechaty.puppet.ResourceBox
import wechaty.puppet.ResourceBox.ResourceBoxType
import wechaty.puppet.schemas.Contact.ContactPayload
import wechaty.puppet.schemas.Puppet
import wechaty.puppet.support.ContactSupport

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
trait ContactRawSupport {
  self: GrpcSupport with ContactSupport =>

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

  override def contactAvatar(contactId: String): ResourceBox = {
    val request = Contact.ContactAvatarRequest.newBuilder()
      .setId(contactId)
      .build()

    val response = grpcClient.contactAvatar(request)
    val filebox = response.getFilebox.getValue
    val root = Puppet.objectMapper.readTree(filebox)
    val boxType = ResourceBoxType.apply(root.get("boxType").asInt())
    boxType match{
      case ResourceBox.ResourceBoxType.Url =>
        ResourceBox.fromUrl(root.get("remoteUrl").asText())
      case ResourceBox.ResourceBoxType.Base64 =>
        ResourceBox.fromBase64(root.get("name").asText(),root.get("base64").asText())
      case other =>
        throw new UnsupportedOperationException(s"other ${other} type not supported!")
    }
  }

  override def contactAvatar(contactId: String, file: ResourceBox): ResourceBox = {
    val toJsonString = file.toJson()

    val value = StringValue.newBuilder().setValue(toJsonString)

    val request = Contact.ContactAvatarRequest.newBuilder()
      .setId(contactId)
      .setFilebox(value)
      .build()

    grpcClient.contactAvatar(request)

    file
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
