package wechaty.puppet.events

import wechaty.puppet.schemas.Puppet.PuppetEventName

/**
  * global event
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
trait EventEmitter {
  type Listener[T] = T => Unit
  private var listeners: Map[PuppetEventName.Type, List[(Listener[_],Boolean)]] = Map()

  def emit[T](event: PuppetEventName.Type, data: T):Unit={
    val eventListenerOpts = listeners.get(event)
    eventListenerOpts match {
      case Some(eventListeners) =>
          eventListeners.foreach{x =>
            x._1.asInstanceOf[Listener[T]](data)
          }
          listeners += event-> eventListeners.filterNot(x=>x._2)
      case _ =>
      //donothing
    }
  }

  def addListener[T](event: PuppetEventName.Type, listener: Listener[T],once:Boolean=false): Unit = {
    listeners.get(event) match {
      case Some(eventListeners) => listeners += event -> (eventListeners :+ (listener,once))
      case _ => listeners += event -> List((listener,once))
    }
  }
}
