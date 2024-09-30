package xcoin.core.services

import com.typesafe.scalalogging.Logger
import reactor.core.publisher.{Flux, Mono}

object ReactorHelper {
  private val logger = Logger(getClass)
  def returnEmptyOnError[T](source: Mono[T]): Mono[T] = {
    source.onErrorResume(e => {
      logger.error(e.toString, e)
      Mono.empty()
    })
  }

  def returnEmptyOnError[T](source: Flux[T]): Flux[T] = {
    source.onErrorResume(e => {
      logger.error(e.toString, e)
      Mono.empty()
    })
  }

}
