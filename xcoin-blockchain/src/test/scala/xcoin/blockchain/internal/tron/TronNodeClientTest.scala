package xcoin.blockchain.internal.tron


import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import xcoin.blockchain.services.TronApi.{TronNodeClientBuilder, TronNodeClientCustomizer, TronNodeClientNetwork}

object TronNodeClientTest{
  @EnableAutoConfiguration
  class TestConfiguration{
    @Bean
    def customizeTronNodeClient(): TronNodeClientCustomizer = {
      new TronNodeClientCustomizer {
        override def customize(tronNodeClientBuilder: TronNodeClientBuilder): Unit = {
          tronNodeClientBuilder.network(TronNodeClientNetwork.TEST_NILE)
          tronNodeClientBuilder.apiKeys(Array("ba1fcb41-0fd1-47a9-b493-d3667a575c75","49567994-1adc-4297-8659-17f30a753e3b"))
        }
      }
    }
  }
}
