package wechaty.plugins

import io.github.wechaty.grpc.PuppetGrpc
import org.grpcmock.GrpcMock.{calledMethod, times, verifyThat}
import org.junit.jupiter.api.Test
import wechaty.TestBase
import wechaty.user.Message

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-19
  */
class RoomConnectorTest extends TestBase{
  @Test
  def test_connector: Unit ={
    val connectorConfig = RoomConnectorConfig(Array("from"),Array("to"))
    connectorConfig.blacklist = (message:Message)=>{
      true
    }
    instance.use(new RoomConnector(connectorConfig))

    mockRoomMessage(roomId = "from")
    mockMessageSendText()
    emitMessagePayloadEvent("messageId")

    verifyThat(
      calledMethod(PuppetGrpc.getMessageSendTextMethod),
      times(0));

    connectorConfig.blacklist = (message:Message)=>{
      false
    }
    emitMessagePayloadEvent("messageId")
    verifyThat(
      calledMethod(PuppetGrpc.getMessageSendTextMethod),
      times(1));

  }
}
