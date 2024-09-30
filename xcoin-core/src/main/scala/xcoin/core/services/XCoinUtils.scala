package xcoin.core.services

import reactor.core.publisher.Mono

import java.time.Duration
import java.util.concurrent.atomic.AtomicLong
import scala.annotation.unused

object XCoinUtils {
  class AtomicCacheValue[T](cacheDuration: Duration, calculator: => Mono[T]) {
    private val lastCalTime         = new AtomicLong(0)
    private var valueOpt: Option[T] = None

    private def isExpiredAndCompareSet(): Boolean = {
      val last = lastCalTime.get()
      (System.currentTimeMillis() - last > cacheDuration.toMillis
        && lastCalTime.compareAndSet(last, System.currentTimeMillis()))
    }

    def getValue(): Mono[T] = {
      if (isExpiredAndCompareSet() || valueOpt.isEmpty) {
        calculator.map { v =>
          valueOpt = Some(v)
          v
        }
      } else Mono.just(valueOpt.get)
    }
  }

}
