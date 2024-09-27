package xcoin.blockchain

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.{ConditionalOnClass, ConditionalOnMissingBean}
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.{Bean, Configuration, Import, Scope}
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import xcoin.blockchain.internal.tron.TronNodeClient
import xcoin.blockchain.internal.tron.TronNodeClient.DefaultTronNodeClientBuilder
import xcoin.blockchain.services.{TronApi, TronNodeClientBuilder, TronNodeClientCustomizer}

import java.util
import java.util.List

@AutoConfiguration
class TronNodeClientAutoConfiguration {
  @Bean(name=Array("TronNodeClient"))
  def tronNodeClient(builder: TronNodeClientBuilder): TronApi = {
    builder.build()
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
