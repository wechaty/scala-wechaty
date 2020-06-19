package wechaty.plugins

import io.github.wechaty.grpc.PuppetGrpc
import org.junit.jupiter.api.Test
import wechaty.TestBase
import org.grpcmock.GrpcMock._
import wechaty.puppet.schemas.Event.EventMessagePayload
import wechaty.puppet.schemas.Puppet.PuppetEventName


/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-19
  */
class DingDongPluginTest extends TestBase{
  @Test
  def test_no_call: Unit ={
    val config = new DingDongPluginConfig
    config.room = false
    config.dm = false
    config.at = false
    config.self = false
    instance.use(new DingDongPlugin(config))
    mockRoomMessage("#ding")
    mockMessageSendText()

    val payload=new EventMessagePayload
    payload.messageId="messageId"
    instance.puppet.emit(PuppetEventName.MESSAGE,payload)

    verifyThat(
      calledMethod(PuppetGrpc.getMessageSendTextMethod),
      times(0));


    config.room=true
    config.self=true
    mockRoomMessage("#ding @me","newMessage1",Array("me"))
    mockMessagePayloadEvent("newMessage1")
    verifyThat(
      calledMethod(PuppetGrpc.getMessageSendTextMethod),
      times(1));

    //test wrong message
    resetGrpcMock()
    mockRoomMessage("hello @me",roomId="newMessage2",Array("me"))
    mockMessagePayloadEvent("newMessage2")
    mockMessageSendText()
    verifyThat(
      calledMethod(PuppetGrpc.getMessageSendTextMethod),
      times(0));

    //test wrong message
    resetGrpcMock()
    config.self = false
    config.dm =false
    config.at = false
    mockRoomMessage("#ding @me",roomId="newMessage3",Array("me"))
    mockMessageSendText()
    mockMessagePayloadEvent("newMessage3")
    verifyThat(
      calledMethod(PuppetGrpc.getMessageSendTextMethod),
      times(1));

  }
  @Test
  def test_plugin: Unit ={
    val config = new DingDongPluginConfig
    config.room = true
    instance.use(new DingDongPlugin(config))
    mockRoomMessage("#ding")
    mockMessageSendText()

    val payload=new EventMessagePayload
    payload.messageId="messageId"
    instance.puppet.emit(PuppetEventName.MESSAGE,payload)

    verifyThat(
      calledMethod(PuppetGrpc.getMessageSendTextMethod),
      times(1));
  }
}
