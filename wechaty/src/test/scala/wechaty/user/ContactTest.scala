package wechaty.user

import io.github.wechaty.grpc.PuppetGrpc
import io.github.wechaty.grpc.puppet.Contact.{ContactPayloadRequest, ContactPayloadResponse}
import org.grpcmock.GrpcMock._
import org.junit.jupiter.api.{Assertions, Test}
import wechaty.TestBase

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-07
  */

class ContactTest extends TestBase{


  @Test
  def test: Unit ={
    //mock server response
    //request1
    val contactPayloadResponse1 = ContactPayloadResponse.newBuilder()
      .setName("jcai")
      .build()
    val contactPayloadRequest1 = ContactPayloadRequest.newBuilder()
      .setId("contactId")
      .build()
    stubFor(unaryMethod(PuppetGrpc.getContactPayloadMethod)
      .withRequest(contactPayloadRequest1)
      .willReturn(contactPayloadResponse1))
    //request2
    val contactPayloadResponse2 = ContactPayloadResponse.newBuilder()
      .setName("jcai2")
      .build()
    val contactPayloadRequest2 = ContactPayloadRequest.newBuilder()
      .setId("contactId2")
      .build()
    stubFor(unaryMethod(PuppetGrpc.getContactPayloadMethod)
      .withRequest(contactPayloadRequest2)
      .willReturn(contactPayloadResponse2))


    val contact = new Contact("contactId")
    Assertions.assertEquals("jcai",contact.name)

    val contact2 = new Contact("contactId2")
    Assertions.assertEquals("jcai2",contact2.name)
  }
}
