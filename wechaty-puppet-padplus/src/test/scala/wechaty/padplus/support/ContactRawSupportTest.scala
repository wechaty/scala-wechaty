package wechaty.padplus.support

import java.util.concurrent.TimeUnit

import org.grpcmock.GrpcMock.{stubFor, unaryMethod}
import org.junit.jupiter.api.{Assertions, Test}
import wechaty.padplus.PadplusTestEventBase
import wechaty.padplus.grpc.PadPlusServerGrpc
import wechaty.padplus.grpc.PadPlusServerOuterClass.{ResponseObject, ResponseType}
import wechaty.padplus.schemas.ModelContact.GrpcContactPayload
import wechaty.puppet.schemas.Contact.ContactPayload

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}



/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-07-01
  */
class ContactRawSupportTest extends PadplusTestEventBase{

  @Test
  def testGetContact: Unit ={
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
  }
}
