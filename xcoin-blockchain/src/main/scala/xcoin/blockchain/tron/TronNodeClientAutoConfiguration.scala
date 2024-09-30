package xcoin.blockchain.tron

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.{ConditionalOnClass, ConditionalOnMissingBean}
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.{Bean, Configuration, Import, Lazy, Scope}
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.tron.trident.api.ReactorWalletGrpc.ReactorWalletStub
import org.tron.trident.api.ReactorWalletSolidityGrpc.ReactorWalletSolidityStub
import xcoin.blockchain.tron.internal.TronNodeClient
import xcoin.blockchain.tron.internal.TronNodeClient.DefaultTronNodeClientBuilder
import xcoin.blockchain.tron.services.TronApi.{TronNodeClientBuilder, TronNodeClientCustomizer}
import xcoin.blockchain.tron.services.TronApi

import java.util
import java.util.List

@AutoConfiguration
class TronNodeClientAutoConfiguration {
  @Bean
  def tronNodeClient(builder: TronNodeClientBuilder,walletStub: ReactorWalletStub,walletSolidityStub: ReactorWalletSolidityStub): TronApi= {
    builder.buildTronNodeClient(walletStub,walletSolidityStub)
  }

  @Bean
  def reactorWalletStub(builder: TronNodeClientBuilder): ReactorWalletStub = {
    builder.buildReactorWalletStub()
  }

  @Bean
  @Lazy
  def reactorWalletSolidityStub(builder: TronNodeClientBuilder): ReactorWalletSolidityStub = {
    builder.buildReactorWalletSolidityStub()
  }

  @Bean
  @Scope("prototype")
  @ConditionalOnMissingBean
  def tronNodeClientBuilder(applicationContext: ApplicationContext, customizers: util.List[TronNodeClientCustomizer]) = {
    val builder = new DefaultTronNodeClientBuilder
    customize(builder, customizers)
    builder
  }

  private def customize(builder: TronNodeClientBuilder, customizers: util.List[TronNodeClientCustomizer]): Unit = {
    customizers.stream().forEach(_.customize(builder))
  }
}
