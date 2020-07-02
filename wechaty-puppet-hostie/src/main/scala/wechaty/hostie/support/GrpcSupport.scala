package wechaty.hostie.support

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{CountDownLatch, Executors, TimeUnit}

import com.typesafe.scalalogging.LazyLogging
import io.github.wechaty.grpc.PuppetGrpc
import io.github.wechaty.grpc.puppet.Contact.ContactPayloadRequest
import io.github.wechaty.grpc.puppet.{Base, Contact}
import io.github.wechaty.grpc.puppet.Event.EventRequest
import io.grpc.stub.ClientCalls.asyncUnaryCall
import io.grpc.stub.{ClientCalls, StreamObserver}
import io.grpc.{ClientCall, ManagedChannel, ManagedChannelBuilder, MethodDescriptor}
import wechaty.hostie.PuppetHostie
import wechaty.puppet.schemas.Contact.ContactPayload

import scala.concurrent.{Future, Promise}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
trait GrpcSupport {
  self: PuppetHostie with ContactRawSupport with MessageRawSupport with LazyLogging =>
  private val executorService = Executors.newSingleThreadScheduledExecutor()
  //from https://github.com/wechaty/java-wechaty/blob/master/wechaty-puppet/src/main/kotlin/Puppet.kt
  private val HEARTBEAT_COUNTER = new AtomicLong()
  private val HOSTIE_KEEPALIVE_TIMEOUT = 15 * 1000L
  private val DEFAULT_WATCHDOG_TIMEOUT = 60L
  protected var grpcClient     : PuppetGrpc.PuppetBlockingStub = _
  protected var asyncGrpcClient: PuppetGrpc.PuppetStub         = _
  protected var channel        : ManagedChannel                = _

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
          logger.warn("ding exception:{}", e.getMessage)
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
        this.channel = ManagedChannelBuilder.forTarget(endpoint)
          .maxInboundMessageSize(1024 * 1024 * 150)
          .usePlaintext().build()
    }
  }

  protected def reconnectStream() {
    logger.info("reconnect stream stream...")
    try {
      stopGrpc()
    } catch {
      case e: Throwable =>
        logger.warn("fail to stop grpc {}", e.getMessage)
    }
    internalStartGrpc()
    logger.info("reconnect stream stream done")

  }

  private def internalStartGrpc() {
    logger.info("start grpc client ....")
    this.grpcClient = PuppetGrpc.newBlockingStub(channel)
    startStream()

    this.grpcClient.start(Base.StartRequest.newBuilder().build())

    try{
      //sometime the grpc can't work well,so logout before start bot
//      this.grpcClient.logout(Base.LogoutRequest.newBuilder().build())
    }catch{
      case e:Throwable=>
        logger.warn(e.getMessage)
    }
    logger.info("start grpc client done")
  }

  private def startStream() {
    this.asyncGrpcClient = PuppetGrpc.newStub(channel)
    val startRequest = EventRequest.newBuilder().build()
    this.asyncGrpcClient.event(startRequest, this)
  }

  protected def stopGrpc(): Unit = {
    logger.info("shutdown Grpc...")
    if(option.channelOpt.isEmpty) {  //if no test!
      //stop stream
      stopStream()

      //stop grpc client
      this.grpcClient.stop(Base.StopRequest.getDefaultInstance)

    }
    executorService.shutdown()
    executorService.awaitTermination(5,TimeUnit.SECONDS)
    this.channel.shutdownNow()
    this.channel.awaitTermination(5,TimeUnit.SECONDS)
  }

  private def stopStream(): Unit = {
    try {
      val countDownLatch = new CountDownLatch(1)
      this.asyncGrpcClient.stop(Base.StopRequest.getDefaultInstance, new StreamObserver[Base.StopResponse] {
        override def onNext(v: Base.StopResponse): Unit = {}

        override def onError(throwable: Throwable): Unit = {}

        override def onCompleted(): Unit = {countDownLatch.countDown()}
      })
      countDownLatch.await()
    } catch {
      case e: Throwable =>
        logger.warn("fail to stop stream {}", e.getMessage)
    }
  }

  type ClientCallback[RespT,T]=RespT => T
  protected def asyncCall[ReqT,RespT](call: MethodDescriptor[ReqT, RespT], req: ReqT): Future[RespT]= {
    asyncCallback(call,req)(resp=> resp)
  }
  def asyncCallback[ReqT, RespT,T](callMethod: MethodDescriptor[ReqT, RespT], req: ReqT)(callback:ClientCallback[RespT,T]): Future[T]= {
    val call = channel.newCall(callMethod,asyncGrpcClient.getCallOptions)
    val promise = Promise[T]
    ClientCalls.asyncUnaryCall(call,req,new StreamObserver[RespT] {
      override def onNext(value: RespT): Unit = {
        val result = callback(value)
        promise.success(result)
      }
      override def onError(t: Throwable): Unit = promise.failure(t)
      override def onCompleted(): Unit = {
        if(!promise.isCompleted) promise.failure(new IllegalStateException("server completed"))
      }
    })
    promise.future
  }
}
