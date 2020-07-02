package wechaty.padplus.support

import org.grpcmock.GrpcMock.{stubFor, unaryMethod}
import org.junit.jupiter.api.{Assertions, Test}
import wechaty.padplus.PadplusTestEventBase
import wechaty.padplus.grpc.PadPlusServerGrpc
import wechaty.padplus.grpc.PadPlusServerOuterClass.{ResponseObject, ResponseType}
import wechaty.padplus.schemas.GrpcSchemas.GrpcMessagePayload
import wechaty.padplus.schemas.PadplusEnums.PadplusMessageType

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-07-02
  */
class MessageRawSupportTest extends PadplusTestEventBase {
  @Test
  def test_sendMessage: Unit ={
    val responseBuilder = ResponseObject.newBuilder.setResult("success")
    stubFor(unaryMethod(PadPlusServerGrpc.getRequestMethod)
      .willReturn(responseBuilder.build()))

    val future = instance.messageSendText("roomId","test",Array())

    val grpcContact = new GrpcMessagePayload
    grpcContact.MsgId= "msgId"
    grpcContact.MsgType = PadplusMessageType.Text.id
    mockEvent(ResponseType.REQUEST_RESPONSE -> grpcContact)

    val payload = Await.result(future, 10 seconds)
    Assertions.assertEquals("msgId", payload)


  }
}
