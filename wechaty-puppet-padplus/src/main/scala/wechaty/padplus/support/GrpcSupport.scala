package wechaty.padplus.support

import java.io.InputStream
import java.util.UUID
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{Executors, TimeUnit}

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.{CannedAccessControlList, GeneratePresignedUrlRequest, ObjectMetadata, PutObjectRequest}
import com.fasterxml.jackson.databind.JsonNode
import com.typesafe.scalalogging.LazyLogging
import io.grpc.stub.{ClientCalls, StreamObserver}
import io.grpc.{ManagedChannel, ManagedChannelBuilder, MethodDescriptor}
import wechaty.padplus.PuppetPadplus
import wechaty.padplus.grpc.PadPlusServerGrpc
import wechaty.padplus.grpc.PadPlusServerOuterClass._
import wechaty.puppet.schemas.Puppet

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-21
  */
trait GrpcSupport {
  self: PuppetPadplus with LazyLogging =>
  private            val executorService                                       = Executors.newSingleThreadScheduledExecutor()
  //from https://github.com/wechaty/java-wechaty/blob/master/wechaty-puppet/src/main/kotlin/Puppet.kt
  private            val HEARTBEAT_COUNTER                                     = new AtomicLong()
  private            val HOSTIE_KEEPALIVE_TIMEOUT                              = 15 * 1000L
  private            val DEFAULT_WATCHDOG_TIMEOUT                              = 60L
  //  protected var grpcClient: PadPlusServerGrpc.PadPlusServerBlockingStub= _
  private   var asyncGrpcClient          : PadPlusServerGrpc.PadPlusServerStub = _
  protected var channel                  : ManagedChannel                      = _
  protected implicit val executionContext: ExecutionContext                    = scala.concurrent.ExecutionContext.Implicits.global


  protected def startGrpc(endpoint: String): Unit = {
    initChannel(endpoint)
    internalStartGrpc()
    //from https://github.com/wechaty/java-wechaty/blob/master/wechaty-puppet/src/main/kotlin/Puppet.kt
    executorService.scheduleAtFixedRate(() => {
      try {
        asyncRequest[JsonNode](ApiType.HEARTBEAT)
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
    //    this.grpcClient = PadPlusServerGrpc.newBlockingStub(channel)
    this.asyncGrpcClient = PadPlusServerGrpc.newStub(channel)
    //    startStream()
    logger.info("start grpc client done")
  }

  private[wechaty] def startStream() {
    val initConfig = InitConfig.newBuilder().setToken(option.token.get).build()
    this.asyncGrpcClient.init(initConfig, this)
  }

  protected def stopGrpc(): Unit = {
    if (option.channelOpt.isEmpty) { //if no test!
      //stop stream
      stopStream()

      //stop grpc client
      //      this.grpcClient.request(RequestObject.newBuilder().setApiType(ApiType.CLOSE).setToken(option.token.get).build())
      this.channel.shutdownNow()
    }
  }

  private def stopStream(): Unit = {
    //do nothing
  }

//  protected def syncRequest[T: TypeTag](apiType: ApiType, data: Option[Any] = None)(implicit classTag: ClassTag[T]): T = {
//    val future = asyncRequest[T](apiType, data)
//    Await.result(future, 10 seconds)
//  }

  protected def generateTraceId(apiType: ApiType): String = {
    UUID.randomUUID().toString
  }

  //can't create Promise[Nothing] instance,so use the method create Future[Unit]
  protected def asyncRequestNothing(apiType: ApiType, data: Option[Any] = None): Future[Unit] = {
    val request = RequestObject.newBuilder()
    request.setToken(option.token.get)
    uinOpt match {
      case Some(id) =>
        request.setUin(id)
      case _ =>
    }
    request.setApiType(apiType)
    data match {
      case Some(str: String) =>
        request.setParams(str)
      case Some(d) =>
        request.setParams(Puppet.objectMapper.writeValueAsString(d))
      case _ =>
    }

    val future = asyncCall(PadPlusServerGrpc.getRequestMethod, request.build())
    future.map { rep =>
      if (rep.getResult != "success") {
        logger.warn("fail to request {}", rep)
        throw new IllegalAccessException("fail to request ,grpc result:" + rep)
      }
    }
  }

  protected def asyncRequest[T: TypeTag](apiType: ApiType, data: Option[Any] = None)(implicit classTag: ClassTag[T]): Future[T] = {
    typeOf[T] match {
      case t if t =:= typeOf[Nothing] =>
        throw new IllegalAccessException("generic type is nothing,maybe you should use asyncRequestNothing !")
      case t if t =:= typeOf[RuntimeClass] =>
        throw new IllegalAccessException("generic type is nothing,maybe you should use asyncRequestNothing !")
      case other =>
        logger.debug(s"async request generic type is $other")
    }
    val request = RequestObject.newBuilder()
    request.setToken(option.token.get)
    uinOpt match {
      case Some(id) =>
        request.setUin(id)
      case _ =>
    }
    request.setApiType(apiType)
    data match {
      case Some(str: String) =>
        request.setParams(str)
      case Some(d) =>
        request.setParams(Puppet.objectMapper.writeValueAsString(d))
      case _ =>
    }
    val requestId = UUID.randomUUID().toString
    request.setRequestId(requestId)
    val traceId = generateTraceId(apiType)
    request.setTraceId(traceId)
    logger.debug("request:{}", request.build())

    val callbackPromise = Promise[StreamResponse]
    CallbackHelper.pushCallbackToPool(traceId, callbackPromise)
    val future = asyncCall(PadPlusServerGrpc.getRequestMethod, request.build())
    future.flatMap { rep =>
      if (rep.getResult != "success") {
        logger.warn("fail to request:{}", rep)
        callbackPromise.failure(new IllegalAccessException("fail to request ,grpc result:" + rep))
      }
      callbackPromise.future
    }.map { streamResponse =>
      typeOf[T] match {
        case t if t =:= typeOf[JsonNode] =>
          Puppet.objectMapper.readTree(streamResponse.getData).asInstanceOf[T]
        case _ =>
          Puppet.objectMapper.readValue(streamResponse.getData, classTag.runtimeClass).asInstanceOf[T]
      }
    }
  }

  type ClientCallback[RespT, T] = RespT => T

  protected def asyncCall[ReqT, RespT](call: MethodDescriptor[ReqT, RespT], req: ReqT): Future[RespT] = {
    asyncCallback(call, req)(resp => resp)
  }

  def asyncCallback[ReqT, RespT, T](callMethod: MethodDescriptor[ReqT, RespT], req: ReqT)(callback: ClientCallback[RespT, T]): Future[T] = {
    val call    = channel.newCall(callMethod, asyncGrpcClient.getCallOptions)
    val promise = Promise[T]
    ClientCalls.asyncUnaryCall(call, req, new StreamObserver[RespT] {
      override def onNext(value: RespT): Unit = {
        val result = callback(value)
        promise.success(result)
      }

      override def onError(t: Throwable): Unit = promise.failure(t)

      override def onCompleted(): Unit = {
        if (!promise.isCompleted) promise.failure(new IllegalStateException("server completed"))
      }
    })
    promise.future
  }

  private val ACCESS_KEY_ID     = "AKIA3PQY2OQG5FEXWMH6"
  private val BUCKET            = "macpro-message-file"
  private val EXPIRE_TIME       = 3600 * 24 * 3
  private val PATH              = "image-message"
  private val SECRET_ACCESS_KEY = "jw7Deo+W8l4FTOL2BXd/VubTJjt1mhm55sRhnsEn"
  //  private val s3 = new AmazonS3Client(new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_ACCESS_KEY));
  private val s3                = AmazonS3ClientBuilder.standard()
    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(ACCESS_KEY_ID, SECRET_ACCESS_KEY)))
    .enablePayloadSigning()
    .withRegion(Regions.CN_NORTHWEST_1).build(); // 此处根据自己的 s3 地区位置改变

  def uploadFile(filename: String, stream: InputStream) {
    //    ACL: "public-read",
    //    const s3 = new AWS.S3({ region: "cn-northwest-1", signatureVersion: "v4" })
    val meta       = new ObjectMetadata
    val key        = PATH + "/" + filename
    val params     = new PutObjectRequest(BUCKET, key, stream, meta)
    val result     = s3.putObject(params.withCannedAcl(CannedAccessControlList.PublicRead));
    //获取一个request
    val urlRequest = new GeneratePresignedUrlRequest(BUCKET, key);
    //生成公用的url
    s3.generatePresignedUrl(urlRequest);
  }
}
