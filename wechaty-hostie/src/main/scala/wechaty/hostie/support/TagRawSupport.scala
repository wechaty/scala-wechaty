package wechaty.hostie.support

import com.google.protobuf.StringValue
import io.github.wechaty.grpc.puppet.{Contact, Tag}
import wechaty.puppet.Puppet

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-06
  */
trait TagRawSupport {
  self: Puppet with GrpcSupport =>
  override def tagContactAdd(tagId: String, contactId: String): Unit = {
    val request = Tag.TagContactAddRequest.newBuilder()
      .setId(tagId)
      .setContactId(contactId)
      .build()

    grpcClient.tagContactAdd(request)

  }

  override def tagContactDelete(tagId: String): Unit = {
    val request = Tag.TagContactDeleteRequest.newBuilder()
      .setId(tagId)
      .build()
    grpcClient.tagContactDelete(request)
  }

  override def tagContactList(contactId: String): Array[String] = {
    val stringValue = StringValue.newBuilder()
      .setValue(contactId)
      .build()


    val request = Tag.TagContactListRequest.newBuilder()
      .setContactId(stringValue)
      .build()
    val contactList = grpcClient.tagContactList(request)
    contactList.getIdsList.toArray(Array[String]())
  }

  override def tagContactList(): Array[String] = {
    val request = Contact.ContactListRequest.newBuilder().build()
    val contactList = grpcClient.contactList(request)
    contactList.getIdsList.toArray(Array[String]())
  }

  override def tagContactRemove(tagId: String, contactId: String): Unit = {
    val request = Tag.TagContactRemoveRequest.newBuilder()
      .setId(tagId)
      .setContactId(contactId)
      .build()
    grpcClient.tagContactRemove(request)
  }
}
