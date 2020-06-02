package wechaty.puppet.events

import wechaty.puppet.schemas.Events.EventName
import wechaty.puppet.schemas.Events.EventName._

/**
  * global event
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
object EventEmitter{
  type Listener[T] = T => Unit
  private var listeners:Map[EventName.Type,List[Listener[_]]] = Map()
  type Converter[T,B] = T => B
  def emit[T,B](event:EventName.Type,data:T)(implicit converter:Converter[T,B]): Unit ={
    println(event,data)
    val eventListenerOpts = listeners.get(event)
    eventListenerOpts match{
      case Some(eventListeners) =>
        event match{
          case PuppetEventNameLogin |PuppetEventNameMessage =>
            eventListeners.foreach{listener=>
              listener.asInstanceOf[Listener[B]](converter(data))
            }
          case _ =>
            eventListeners.foreach(x=>x.asInstanceOf[Listener[T]](data))
        }
      case _ =>
        //donothing
    }
  }
  def addListener[T](event:EventName.Type,listener:Listener[T]): Unit ={
    listeners.get(event) match {
      case Some(eventListeners) => listeners += event-> (eventListeners :+ listener)
      case _ => listeners += event -> List(listener)
    }
  }
}
