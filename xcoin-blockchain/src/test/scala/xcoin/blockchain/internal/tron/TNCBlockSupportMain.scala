package xcoin.blockchain.internal.tron

import org.springframework.boot.test.context.runner.ApplicationContextRunner
import xcoin.blockchain.internal.tron.TronNodeClientTest.TestQuickNodeConfiguration
import xcoin.blockchain.services.TronApi

object TNCBlockSupportMain {
  def main(args: Array[String]): Unit = {
    //测试在QuickNode的Grpc服务器
    val contextRunner = new ApplicationContextRunner()
      .withUserConfiguration(classOf[TestQuickNodeConfiguration])
    contextRunner.withPropertyValues("background.enabled=false",
                                     "is_test_net=false",
                                     "xcoin.blockchain.quicknode.enabled=true",
                                     "spring.liquibase.enabled=false").run { boot =>
      val api = boot.getBean(classOf[TronApi])
      api.blockLatestId().flatMapMany{nowId=>
        api.blockEventStream(nowId)
      }.blockLast()
    }
  }
}
