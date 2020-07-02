package wechaty.padplus.support

import org.grpcmock.GrpcMock.{stubFor, unaryMethod}
import org.junit.jupiter.api.{Assertions, Test}
import wechaty.padplus.PadplusTestEventBase
import wechaty.padplus.grpc.PadPlusServerGrpc
import wechaty.padplus.grpc.PadPlusServerOuterClass.{ResponseObject, ResponseType}
import wechaty.padplus.schemas.ModelContact.GrpcContactPayload

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.{Failure, Success}



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

    val contactId = "contactId"
    val future = instance.contactPayload(contactId)


    val grpcContact = new GrpcContactPayload
    grpcContact.UserName=contactId
    grpcContact.Signature="jcai"
    mockEvent(ResponseType.CONTACT_LIST->grpcContact)

    val payload = Await.result(future,10 seconds)
    Assertions.assertEquals(contactId,payload.id)
  }
  @Test
  def testGetContactFail: Unit ={
    val responseBuilder = ResponseObject.newBuilder.setResult("fail")
    stubFor(unaryMethod(PadPlusServerGrpc.getRequestMethod)
      .willReturn(responseBuilder.build())
    )

    val contactId = "contactId"
    val future    = instance.contactPayload(contactId)
    val payload   = Await.ready(future,10 seconds)
    payload.value.get match {
      case Success(v) =>
        Assertions.fail("can't reach here")
      case Failure(e) =>
    }
  }
}
