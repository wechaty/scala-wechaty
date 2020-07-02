package wechaty.plugins

import io.github.wechaty.grpc.PuppetGrpc
import io.github.wechaty.grpc.puppet.Friendship.FriendshipType
import org.grpcmock.GrpcMock.{calledMethod, times, verifyThat}
import org.junit.jupiter.api.Test
import wechaty.TestBase

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-19
  */
class FriendshipAcceptorTest extends TestBase{
  @Test
  def test_add: Unit ={
    val config = FriendshipAcceptorConfig()
    instance.use(new FriendshipAcceptor(config,isWait = true))

    mockFriendshipAdd()
    emitFriendshipAddPayloadEvent()

    verifyThat(
      calledMethod(PuppetGrpc.getFriendshipAcceptMethod),
      times(1));


    //wrong friend
    resetGrpcMock()
    config.keywordOpt=Some("test")
    mockFriendshipAdd()
    emitFriendshipAddPayloadEvent()
    verifyThat(
      calledMethod(PuppetGrpc.getFriendshipAcceptMethod),
      times(0));
    //correct friend
    resetGrpcMock()
    config.keywordOpt=Some("test")
    mockFriendshipAdd("hello test")
    emitFriendshipAddPayloadEvent("fid")
    verifyThat(
      calledMethod(PuppetGrpc.getFriendshipAcceptMethod),
      times(1));

  }
  @Test
  def testGreetings: Unit ={
    val config = FriendshipAcceptorConfig()
    instance.use(new FriendshipAcceptor(config,isWait = true))
    config.greeting="你好"

    mockFriendshipAdd(payloadType = FriendshipType.FRIENDSHIP_TYPE_CONFIRM)
    mockMessageSendText()
    emitFriendshipAddPayloadEvent()
    verifyThat(
      calledMethod(PuppetGrpc.getFriendshipAcceptMethod),
      times(0));
    verifyThat(
      calledMethod(PuppetGrpc.getMessageSendTextMethod),
      times(1));
  }
}
