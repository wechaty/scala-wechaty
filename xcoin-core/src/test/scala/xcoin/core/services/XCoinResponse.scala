package xcoin.core.services

import com.typesafe.scalalogging.LazyLogging
import io.swagger.v3.oas.annotations.media.Schema
import reactor.core.publisher.Mono

import scala.language.implicitConversions

object XCoinResponse extends LazyLogging {
  val OK: Mono[XCoinResponse[Void]] = Mono.just(new XCoinResponse[Void])

  class XCoinResponse[T] {
    @Schema(description="成功返回0，其他均为错误")
    var code   : Int    = 0
    @Schema(description="发生错误时候返回的消息")
    var msg    : String = _
    var subCode: String = _
    var subMsg : String = _
    var data   : T      = _
  }

  @Schema(title="XCoinApiListResponse")
  object XCoinListResponse {
    def from[T](list: java.util.List[T], total: Long): XCoinListResponse[T] = {
      val response = new XCoinListResponse[T]
      response.data = list
      response.total = total.intValue()
      response
    }
  }

  @Schema(title="XCoinApiListResponse")
  class XCoinListResponse[T] extends XCoinResponse[java.util.List[T]] {
    @Schema(title="数据总长度")
    var total: Int = _
  }


  implicit def toXCoinApiListResponse[T](response: (java.util.List[T], Long)): Mono[XCoinListResponse[T]] = {
    val ret = XCoinListResponse.from(response._1, response._2)
    Mono.just(ret)
  }

  implicit def toXCoinApiMonoResponse[T](response: Mono[T]): Mono[XCoinResponse[T]] = {
    response.map(r => {
      val ret = new XCoinResponse[T]
      ret.data = r
      ret
    })
  }


  implicit def toXCoinApiResponse[T](response: T): Mono[XCoinResponse[T]] = {
    val ret = new XCoinResponse[T]
    ret.data = response
    Mono.just(ret)
  }

  implicit def toJavaVoid(origin: Mono[Unit]): Mono[XCoinResponse[Void]] = {
    origin.flatMap(_ => OK)
  }

  implicit def toJavaVoid(origin: Unit): Mono[XCoinResponse[Void]] = {
    OK
  }

  private val isEnableLog = sys.props.getOrElse("enable-log", "false").toBoolean

}
