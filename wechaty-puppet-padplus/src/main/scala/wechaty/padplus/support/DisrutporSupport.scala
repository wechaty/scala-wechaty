package wechaty.padplus.support

import java.util.concurrent.{Executors, TimeUnit}

import com.lmax.disruptor.dsl.{Disruptor, ProducerType}
import com.lmax.disruptor.{BusySpinWaitStrategy, EventFactory, EventHandler, ExceptionHandler}
import com.typesafe.scalalogging.LazyLogging
import wechaty.puppet.events.EventEmitter
import wechaty.puppet.schemas.Puppet.PuppetEventName
import wechaty.puppet.schemas.Puppet.PuppetEventName.Type

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-30
  */
trait DisrutporSupport extends EventEmitter {
  self :LazyLogging =>
  private class EventDef {
    var eventName: PuppetEventName.Type = _
    var value    : Any = _
  }
  private var disruptor:Disruptor[EventDef] = _
  protected def startDisruptor(): Unit ={
    disruptor = new Disruptor[EventDef](
      new EventFactory[EventDef]() {
        @Override
        def newInstance():EventDef= {
          new EventDef()
        }
      },
      1 << 32,
      Executors.defaultThreadFactory(),
      ProducerType.SINGLE,
      new BusySpinWaitStrategy()
    )
    disruptor.handleEventsWith(new EventHandler[EventDef](){
      override def onEvent(event: EventDef, sequence: Long, endOfBatch: Boolean): Unit = {
        logger.debug("emit event using super {}",event.eventName)
        DisrutporSupport.super.emit(event.eventName,event.value)
      }
    })
    disruptor.setDefaultExceptionHandler(new ExceptionHandler[EventDef] {
      override def handleEventException(ex: Throwable, sequence: Long, event: EventDef): Unit = {
        logger.error(ex.getMessage,ex)
      }

      override def handleOnStartException(ex: Throwable): Unit = {
        throw ex
      }

      override def handleOnShutdownException(ex: Throwable): Unit = {
        logger.error(ex.getMessage,ex)
      }
    })
    disruptor.start()
  }
  override def emit[T](eventName: Type, value: T): Unit = {
    logger.debug("publish event {}",eventName)
    disruptor.publishEvent((event: EventDef, sequence: Long) => {
      event.eventName = eventName
      event.value = value
    })
  }
  protected def shutdownDisruptor(): Unit ={
    disruptor.shutdown(5,TimeUnit.SECONDS);
  }
}
