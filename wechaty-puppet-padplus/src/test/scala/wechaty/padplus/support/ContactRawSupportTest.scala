package wechaty.padplus.support

import org.junit.jupiter.api.Test
import wechaty.padplus.PadplusTestEventBase



/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-07-01
  */
class ContactRawSupportTest extends PadplusTestEventBase{

  @Test
  def testGetContact: Unit ={
    /*
    val responseBuilder = ResponseObject.newBuilder.setResult("success")
    stubFor(unaryMethod(PadPlusServerGrpc.getRequestMethod)
      .willReturn(responseBuilder.build())
    )

    val future = Future[ContactPayload] {
      val contactId = "contactId"
      instance.contactPayload(contactId)
    }

    Thread.sleep(TimeUnit.SECONDS.toMillis(2))

    val grpcContact = new GrpcContactPayload
    grpcContact.UserName="jcai"
    grpcContact.Signature="jcai"
    mockEvent(ResponseType.CONTACT_SEARCH->grpcContact)

    val payload = Await.result(future,10 seconds)
    Assertions.assertEquals("jcai",payload.id)
    awaitEventCompletion()

     */
  }
}
