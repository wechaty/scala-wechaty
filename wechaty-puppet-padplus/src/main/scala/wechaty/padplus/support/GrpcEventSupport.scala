package wechaty.padplus.support

import java.util.concurrent.{CountDownLatch, TimeUnit}

import io.grpc.stub.StreamObserver
import wechaty.padplus.PuppetPadplus
import wechaty.padplus.grpc.PadPlusServerOuterClass.{ResponseType, StreamResponse}
import wechaty.puppet.schemas.Puppet.isBlank

import scala.util.{Failure, Success, Try}

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-21
  */
trait GrpcEventSupport extends StreamObserver[StreamResponse]{
  self: PuppetPadplus =>

  protected var selfId:Option[String] = None
  private val countDownLatch = new CountDownLatch(1)


  protected def awaitStreamStart()=countDownLatch.await(10,TimeUnit.SECONDS)
  override def onNext(response: StreamResponse): Unit = {
    logger.debug("stream response:{}",response)
    countDownLatch.countDown()
    saveUin(response.getUinBytes)

    val traceId = response.getTraceId
    if(!isBlank(traceId)){
      val callback = callbackPool.getIfPresent(traceId)
      if(callback != null){
        callback(response)
      }
    }else {
      Try {
        val partialFunction = sysPartialFunction(response) orElse
          loginPartialFunction(response) orElse
          messagePartialFunction(response)
        partialFunction.applyOrElse(response.getResponseType, { _: ResponseType => Unit })
      } match {
        case Success(_)=>
        case Failure(exception) =>
          logger.error(exception.getMessage,exception)
      }
    }
 }

  override def onError(throwable: Throwable): Unit = {
    logger.error(throwable.getMessage,throwable)
  }

  override def onCompleted(): Unit = {
    logger.info("completed")
  }
}
