package xcoin.blockchain.tron.internal

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import xcoin.blockchain.tron.services.TronApi.{TronNodeClientBuilder, TronNodeClientCustomizer, TronNodeClientNetwork}
import xcoin.blockchain.tron.services.TronApi.{TronNodeClientBuilder, TronNodeClientCustomizer, TronNodeClientNetwork}

object TronNodeClientTest{
  @EnableAutoConfiguration
  class TestNileConfiguration {
    @Bean
    def customizeTronNodeClient(): TronNodeClientCustomizer = {
      new TronNodeClientCustomizer {
        override def customize(tronNodeClientBuilder: TronNodeClientBuilder): Unit = {
          tronNodeClientBuilder.network(TronNodeClientNetwork.TEST_NILE)
          tronNodeClientBuilder.apiKeys(Array("ba1fcb41-0fd1-47a9-b493-d3667a575c75", "49567994-1adc-4297-8659-17f30a753e3b"))
        }
      }
    }
  }

  @EnableAutoConfiguration
  class TestQuickNodeConfiguration{
    @Bean
    def customizeTronNodeClient(): TronNodeClientCustomizer = {
      new TronNodeClientCustomizer {
        override def customize(tronNodeClientBuilder: TronNodeClientBuilder): Unit = {
          tronNodeClientBuilder.network(TronNodeClientNetwork.MAIN_QUICK_NODE)
          tronNodeClientBuilder.quickNodeKey("e04742def8ea3c44576ed57ef8aa277d4ae66eb0")
        }
      }
    }
  }
}
