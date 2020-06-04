package wechaty.puppet.events

import wechaty.puppet.schemas.Events.{PuppetEventName, EventPayload}

/**
  * global event
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
object EventEmitter {
  type Listener[T<:EventPayload] = T => Unit
  private var listeners: Map[PuppetEventName.Type, List[Listener[_<:EventPayload]]] = Map()

  def emit[T<:EventPayload](event: PuppetEventName.Type, data: T):Unit={
    val eventListenerOpts = listeners.get(event)
    eventListenerOpts match {
      case Some(eventListeners) =>
          eventListeners.foreach(x => x.asInstanceOf[Listener[T]](data))
      case _ =>
      //donothing
    }
  }

  def addListener[T<:EventPayload](event: PuppetEventName.Type, listener: Listener[T]): Unit = {
    listeners.get(event) match {
      case Some(eventListeners) => listeners += event -> (eventListeners :+ listener)
      case _ => listeners += event -> List(listener)
    }
  }
}
