package xcoin.blockchain.internal.tron

import com.typesafe.scalalogging.Logger
import io.grpc.Metadata.Key
import io.grpc.{CallOptions, Channel, ClientCall, ClientInterceptor, ForwardingClientCall, ManagedChannelBuilder, Metadata, MethodDescriptor}
import org.tron.trident.api.ReactorWalletGrpc.ReactorWalletStub
import org.tron.trident.api.ReactorWalletSolidityGrpc.ReactorWalletSolidityStub
import org.tron.trident.api.{ReactorWalletGrpc, ReactorWalletSolidityGrpc}
import xcoin.blockchain.internal.tron.TronNodeClient.ApiKeyClientInterceptor
import xcoin.blockchain.services.TronApi
import xcoin.blockchain.services.TronApi.{TronNodeClientBuilder, TronNodeClientNetwork}

import java.util.concurrent.atomic.AtomicLong

class TronNodeClient(protected val stub: ReactorWalletStub,
                     protected val solidityStub: ReactorWalletSolidityStub,
                     protected val network:TronNodeClientNetwork.Type,
                    )
  extends TronApi
    with TNCVoteSupport
    with TNCContractSupport
    with TNCUSDTSupport
    with TNCAccountSupport
    with TNCResourceSupport
    with TNCTransactionSupport {
  protected val logger = Logger[TronNodeClient]
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

    private val logger                                           = Logger[DefaultTronNodeClientBuilder]
    private var network                                          = TronNodeClientNetwork.MAIN
    private var apiKeyClientInterceptor: ApiKeyClientInterceptor = new ApiKeyClientInterceptor(Array())

    def network(network: TronNodeClientNetwork.Type): Unit = {
      this.network = network
    }

    def apiKeys(keys: Array[String]): Unit = {
      this.apiKeyClientInterceptor = new ApiKeyClientInterceptor(keys)
    }

    private def getGrpcEndpoint(): (String, String) = {
      network match {
        case TronNodeClientNetwork.MAIN =>
          ("grpc.trongrid.io:50051", "grpc.trongrid.io:50052")
        case TronNodeClientNetwork.TEST_NILE =>
          ("grpc.nile.trongrid.io:50051", "grpc.nile.trongrid.io:50061")
        case TronNodeClientNetwork.TEST_SHASTA =>
          ("grpc.shasta.trongrid.io:50051", "grpc.shasta.trongrid.io:50052")
      }
    }

    override def buildReactorWalletStub(): ReactorWalletStub = {
      logger.info("creating stub")
      val (grpcEndpoint, _) = getGrpcEndpoint()
      val channel           = ManagedChannelBuilder.forTarget(grpcEndpoint).intercept(apiKeyClientInterceptor).usePlaintext.build()
      ReactorWalletGrpc.newReactorStub(channel)
    }

    override def buildReactorWalletSolidityStub(): ReactorWalletSolidityStub = {
      logger.info("creating stubSolidity")
      val (_, grpcEndpointSolidity) = getGrpcEndpoint()
      val channelSolidity           = ManagedChannelBuilder.forTarget(grpcEndpointSolidity).intercept(apiKeyClientInterceptor).usePlaintext.build()
      ReactorWalletSolidityGrpc.newReactorStub(channelSolidity)
    }

    override def buildTronNodeClient(walletStub: ReactorWalletStub, walletSolidityStub: ReactorWalletSolidityStub): TronApi = {
      new TronNodeClient(walletStub,walletSolidityStub,network)
    }
  }
}

