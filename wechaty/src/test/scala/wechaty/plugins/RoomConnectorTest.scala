package wechaty.plugins

import io.github.wechaty.grpc.PuppetGrpc
import org.grpcmock.GrpcMock.{calledMethod, times, verifyThat}
import org.junit.jupiter.api.Test
import wechaty.TestBase

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-19
  */
class RoomConnectorTest extends TestBase{
  @Test
  def test_connector: Unit ={
    val connectorConfig = RoomConnectorConfig(Array("from"),Array("to"))
    connectorConfig.blacklist = _ => true
    instance.use(new RoomConnector(connectorConfig))

    mockRoomMessage(roomId = "from")
    mockMessageSendText()
    emitMessagePayloadEvent("messageId")

    verifyThat(
      calledMethod(PuppetGrpc.getMessageSendTextMethod),
      times(0));

    connectorConfig.blacklist =  _ => false
    connectorConfig.mapper = (_,_,_) => None
    emitMessagePayloadEvent("messageId")
    verifyThat(
      calledMethod(PuppetGrpc.getMessageSendTextMethod),
      times(0));

    connectorConfig.mapper = (_,msg,_)=>Some(msg)
    emitMessagePayloadEvent("messageId")
    verifyThat(
      calledMethod(PuppetGrpc.getMessageSendTextMethod),
      times(1));

  }
}
