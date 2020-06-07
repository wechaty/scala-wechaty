package wechaty.hostie.support

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{Executors, TimeUnit}

import io.github.wechaty.grpc.PuppetGrpc
import io.github.wechaty.grpc.puppet.Base
import io.github.wechaty.grpc.puppet.Event.EventRequest
import io.grpc.stub.StreamObserver
import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import wechaty.hostie.PuppetHostie
import wechaty.puppet.LoggerSupport

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
trait GrpcSupport {
  self: PuppetHostie with ContactRawSupport with MessageRawSupport with LoggerSupport =>
  private val executorService = Executors.newSingleThreadScheduledExecutor()
  //from https://github.com/wechaty/java-wechaty/blob/master/wechaty-puppet/src/main/kotlin/Puppet.kt
  private val HEARTBEAT_COUNTER = new AtomicLong()
  private val HOSTIE_KEEPALIVE_TIMEOUT = 15 * 1000L
  private val DEFAULT_WATCHDOG_TIMEOUT = 60L
  protected var grpcClient: PuppetGrpc.PuppetBlockingStub = _
  private var eventStream: PuppetGrpc.PuppetStub = _
  protected var channel: ManagedChannel = _

  protected def startGrpc(endpoint: String): Unit = {
    initChannel(endpoint)
    internalStartGrpc()
    //from https://github.com/wechaty/java-wechaty/blob/master/wechaty-puppet/src/main/kotlin/Puppet.kt
    executorService.scheduleAtFixedRate(() => {
      val seq = HEARTBEAT_COUNTER.incrementAndGet()
      try {
        ding(s"heartbeat ...${seq}")
      } catch {
        case e: Throwable =>
          warn("ding exception:{}", e.getMessage)
        //ignore any exception
      }
    }, HOSTIE_KEEPALIVE_TIMEOUT, HOSTIE_KEEPALIVE_TIMEOUT, TimeUnit.MILLISECONDS)

  }

  protected def initChannel(endpoint: String) = {
    option.channelOpt match {
      case Some(channel) =>
        this.channel = channel
      case _ =>
        /*
    this.channel = NettyChannelBuilder
      .forTarget(endpoint)
      .keepAliveTime(20, TimeUnit.SECONDS)
      //      .keepAliveTimeout(2, TimeUnit.SECONDS)
      .keepAliveWithoutCalls(true)
            .idleTimeout(2, TimeUnit.HOURS)
      .enableRetry()
      .usePlaintext().build()
      */
        this.channel = ManagedChannelBuilder.forTarget(endpoint).usePlaintext().build()
    }
  }

  protected def reconnectStream() {
    info("reconnect stream stream...")
    try {
      stopGrpc()
    } catch {
      case e: Throwable =>
        warn("fail to stop grpc {}", e.getMessage)
    }
    internalStartGrpc()
    info("reconnect stream stream done")

  }

  private def internalStartGrpc() {
    info("start grpc client ....")
    this.grpcClient = PuppetGrpc.newBlockingStub(channel)
    startStream()

    this.grpcClient.start(Base.StartRequest.newBuilder().build())
    this.grpcClient.logout(Base.LogoutRequest.newBuilder().build())
    info("start grpc client done")
  }

  private def startStream() {
    this.eventStream = PuppetGrpc.newStub(channel)
    val startRequest = EventRequest.newBuilder().build()
    this.eventStream.event(startRequest, this)
  }

  protected def stopGrpc(): Unit = {
    if(option.channelOpt.isEmpty) {  //if no test!
      //stop stream
      stopStream()

      //stop grpc client
      this.grpcClient.stop(Base.StopRequest.getDefaultInstance)
    }
  }

  private def stopStream(): Unit = {
    try {
      this.eventStream.stop(Base.StopRequest.getDefaultInstance, new StreamObserver[Base.StopResponse] {
        override def onNext(v: Base.StopResponse): Unit = {}

        override def onError(throwable: Throwable): Unit = {}

        override def onCompleted(): Unit = {}
      })
    } catch {
      case e: Throwable =>
        warn("fail to stop stream {}", e.getMessage)
    }
  }
}
