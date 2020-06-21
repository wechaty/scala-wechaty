package wechaty.padplus

import com.typesafe.scalalogging.LazyLogging
import wechaty.puppet.schemas.Puppet.PuppetOptions

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-21
  */
object TestMain extends LazyLogging{
  def main(args: Array[String]): Unit = {
    val token=sys.props.get("PUPPET_PADPLUS_TOKEN")
    val endpoint="padplus.juzibot.com:50051"
    val option = new PuppetOptions
    option.token=token
    option.endPoint=Some(endpoint)
    val padplus = new PuppetPadplus(option)
    padplus.start()

    Thread.currentThread().join()
  }
}
