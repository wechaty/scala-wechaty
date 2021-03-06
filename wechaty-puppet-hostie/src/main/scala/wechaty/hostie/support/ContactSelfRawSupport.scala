package wechaty.hostie.support

import io.github.wechaty.grpc.puppet.{Base, Contact}
import wechaty.puppet.support.ContactSelfSupport

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-06
  */
trait ContactSelfRawSupport {
  self: ContactSelfSupport with GrpcSupport =>
  /**
    *
    * ContactSelf
    *
    */
  override def contactSelfName(name: String): Unit = {
    val request = Contact.ContactSelfNameRequest.newBuilder()
      .setName(name)
      .build()
    grpcClient.contactSelfName(request)
  }

  override def contactSelfQRCode(): String = {
    val request = Contact.ContactSelfQRCodeRequest.newBuilder().build()

    val contactSelfQRCode = grpcClient.contactSelfQRCode(request)
    contactSelfQRCode.getQrcode
  }

  override def contactSelfSignature(signature: String): Unit = {
    val request = Contact.ContactSelfSignatureRequest.newBuilder()
      .setSignature(signature)
      .build()

    grpcClient.contactSelfSignature(request)
  }

  override def logout(): Unit = {
    this.grpcClient.logout(Base.LogoutRequest.newBuilder().build())
  }
}
