package xcoin.blockchain.internal.tron

import com.typesafe.scalalogging.Logger
import io.grpc.Metadata.Key
import io.grpc.{CallOptions, Channel, ClientCall, ClientInterceptor, ForwardingClientCall, ManagedChannelBuilder, Metadata, MethodDescriptor}
import org.tron.trident.api.{ReactorWalletGrpc, ReactorWalletSolidityGrpc}
import xcoin.blockchain.internal.tron.TronNodeClient.ApiKeyClientInterceptor
import xcoin.blockchain.services.{TronApi, TronNodeClientBuilder}
import xcoin.blockchain.services.TronApi.TronNodeClientNetwork

import java.util.concurrent.atomic.AtomicLong

class TronNodeClient(grpcEndpoint:String, grpcEndpointSolidity:String, apiKeys:Array[String]) extends TronApi with TransactionSupportImpl {
  private val logger = Logger[TronNodeClient]
  private lazy val apiKeyClientInterceptor = new ApiKeyClientInterceptor(apiKeys)
  protected lazy val stub                    = {
    logger.info("creating stub")
    val channel = ManagedChannelBuilder.forTarget(grpcEndpoint).intercept(apiKeyClientInterceptor).usePlaintext.build()
    ReactorWalletGrpc.newReactorStub(channel)
  }
  protected lazy val stubSolidity            = {
    logger.info("creating stubSolidity")
    val channelSolidity = ManagedChannelBuilder.forTarget(grpcEndpointSolidity).intercept(apiKeyClientInterceptor).usePlaintext.build()
    ReactorWalletSolidityGrpc.newReactorStub(channelSolidity)
  }

}
object TronNodeClient {
  class ApiKeyClientInterceptor(apiKeys: Array[String]) extends ClientInterceptor {
    private      val logger    = Logger[ApiKeyClientInterceptor]
    private lazy val keyLength = {
      apiKeys.length
    }
    private      val next      = new AtomicLong(0)

    override def interceptCall[ReqT, RespT](methodDescriptor: MethodDescriptor[ReqT, RespT], callOptions: CallOptions, channel: Channel): ClientCall[ReqT, RespT] = {
      new ForwardingClientCall.SimpleForwardingClientCall[ReqT, RespT](channel.newCall(methodDescriptor, callOptions)) {
        override def start(responseListener: ClientCall.Listener[RespT], headers: Metadata): Unit = {
          if (apiKeys.nonEmpty) {
            val apiKey                    = apiKeys((next.getAndIncrement() % keyLength).intValue)
            val key: Metadata.Key[String] = Key.of("TRON-PRO-API-KEY", Metadata.ASCII_STRING_MARSHALLER)
            logger.info("use api_key{} for {}", apiKey, methodDescriptor.getBareMethodName)
            headers.put(key, apiKey)
          }
          super.start(responseListener, headers)
        }
      }
    }
  }

  class DefaultTronNodeClientBuilder extends TronNodeClientBuilder {
    private var network = TronNodeClientNetwork.MAIN
    private var apiKeys = Array[String]()

    def network(network: TronNodeClientNetwork.Type): Unit = {
      this.network = network
    }

    def apiKeys(keys: Array[String]): Unit = {
      this.apiKeys = keys
    }

    def build(): TronApi = {
      val (grpcEndpoint, grpcEndpointSolidity) = {
        network match {
          case TronNodeClientNetwork.MAIN =>
            ("grpc.trongrid.io:50051", "grpc.trongrid.io:50052")
          case TronNodeClientNetwork.TEST_NILE =>
            ("grpc.nile.trongrid.io:50051", "grpc.nile.trongrid.io:50061")
          case TronNodeClientNetwork.TEST_SHASTA =>
            ("grpc.shasta.trongrid.io:50051", "grpc.shasta.trongrid.io:50052")
        }
      }

      new TronNodeClient(grpcEndpoint, grpcEndpointSolidity, this.apiKeys)
    }

  }
}

