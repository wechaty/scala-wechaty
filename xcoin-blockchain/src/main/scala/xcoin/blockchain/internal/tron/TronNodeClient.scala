package xcoin.blockchain.internal.tron

import com.google.protobuf.Message
import com.typesafe.scalalogging.Logger
import io.grpc.Metadata.Key
import io.grpc.{CallOptions, Channel, ClientCall, ClientInterceptor, ForwardingClientCall, Grpc, ManagedChannelBuilder, Metadata, MethodDescriptor, TlsChannelCredentials}
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import org.bouncycastle.util.encoders.Hex
import org.tron.trident.api.ReactorWalletGrpc.ReactorWalletStub
import org.tron.trident.api.ReactorWalletSolidityGrpc.ReactorWalletSolidityStub
import org.tron.trident.api.{ReactorWalletGrpc, ReactorWalletSolidityGrpc}
import xcoin.blockchain.internal.tron.TronNodeClient.ApiKeyClientInterceptor
import xcoin.blockchain.services.TronApi
import xcoin.blockchain.services.TronApi.{TronNodeClientBuilder, TronNodeClientNetwork}

import java.io.{File, FileOutputStream}
import java.util.concurrent.atomic.AtomicLong
import scala.util.Using

class TronNodeClient(protected val stub: ReactorWalletStub,
                     protected val solidityStub: ReactorWalletSolidityStub,
                     protected val network:TronNodeClientNetwork.Type,
                    )
  extends TronApi
    with TNCVoteSupport
    with TNCContractSupport
    with TNCUSDTSupport
    with TNCAccountSupport
    with TNCBlockSupport
    with TNCResourceSupport
    with TNCTransactionSupport {
  protected val logger = Logger[TronNodeClient]
  protected def dumpProtobufMessage(message:Message): Unit = {
    logger.whenDebugEnabled {
      logger.debug("Name:{},Message:{}",message.getClass.getSimpleName, Hex.toHexString(message.toByteArray))
      Using.resource(new FileOutputStream(new File(s"${message.getClass.getSimpleName}-${System.currentTimeMillis()}.bin"))) { os =>
        os.write(message.toByteArray)
      }
    }
  }
}
object TronNodeClient {
  private val TRON_GRID_API_KEY_NAME="TRON-PRO-API-KEY"
  private val QUICK_API_KEY_NAME="x-token"
  class ApiKeyClientInterceptor(apiKeyName:String,apiKeys: Array[String]) extends ClientInterceptor {
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
            val key: Metadata.Key[String] = Key.of(apiKeyName, Metadata.ASCII_STRING_MARSHALLER)
            logger.info("use api_key[{}] for {}", apiKey, methodDescriptor.getBareMethodName)
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
    private var apiKeyClientInterceptor: ApiKeyClientInterceptor = new ApiKeyClientInterceptor(TRON_GRID_API_KEY_NAME,Array())

    def network(network: TronNodeClientNetwork.Type): Unit = {
      this.network = network
    }

    def apiKeys(keys: Array[String]): Unit = {
      this.apiKeyClientInterceptor = new ApiKeyClientInterceptor(TRON_GRID_API_KEY_NAME,keys)
    }


    override def quickNodeKey(key: String): Unit = {
      this.apiKeyClientInterceptor = new ApiKeyClientInterceptor(QUICK_API_KEY_NAME, Array(key))
    }

    private def getGrpcEndpoint(): (String, String) = {
      network match {
        case TronNodeClientNetwork.MAIN =>
          ("grpc.trongrid.io:50051", "grpc.trongrid.io:50052")
        case TronNodeClientNetwork.MAIN_QUICK_NODE =>
          ("solemn-powerful-surf.tron-mainnet.quiknode.pro:50051", "grpc.trongrid.io:50052")
        case TronNodeClientNetwork.TEST_NILE =>
          ("grpc.nile.trongrid.io:50051", "grpc.nile.trongrid.io:50061")
        case TronNodeClientNetwork.TEST_SHASTA =>
          ("grpc.shasta.trongrid.io:50051", "grpc.shasta.trongrid.io:50052")
      }
    }

    override def buildReactorWalletStub(): ReactorWalletStub = {
      logger.info("creating stub")
      val (grpcEndpoint, _) = getGrpcEndpoint()
      val channel:Channel = {
        network match {
          case TronNodeClientNetwork.MAIN_QUICK_NODE =>
            val credentials = TlsChannelCredentials.newBuilder()
              .trustManager(InsecureTrustManagerFactory.INSTANCE.getTrustManagers(): _*)
              .build()
            Grpc.newChannelBuilder(grpcEndpoint, credentials)
              .intercept(apiKeyClientInterceptor).asInstanceOf[ManagedChannelBuilder[_]]
              .build()
          case _ =>
            ManagedChannelBuilder.forTarget(grpcEndpoint)
              .intercept(apiKeyClientInterceptor).asInstanceOf[ManagedChannelBuilder[_]]
              .usePlaintext.asInstanceOf[ManagedChannelBuilder[_]]
              .build()
        }
      }
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

