package xcoin.blockchain.internal.tron

import org.tron.trident.api.GrpcAPI.{BytesMessage, EmptyMessage}
import org.tron.trident.core.ApiWrapper
import org.tron.trident.core.ApiWrapper.parseAddress
import org.tron.trident.utils.Base58Check
import reactor.core.publisher.{Flux, Mono}
import xcoin.blockchain.services.TronApi.VoteSupport

import java.util.stream.Collectors
import scala.jdk.CollectionConverters.ListHasAsScala

/**
 * https://developers.tron.network/docs/super-representatives
 */
trait TNCVoteSupport extends VoteSupport {

  self: TronNodeClient =>
  override def voteList(topN:Int=127): Flux[Witness] = {
    stub
      .listWitnesses(EmptyMessage.getDefaultInstance)
      .flatMapMany { response =>
        Flux
          .fromIterable(response.getWitnessesList)
          .map { wit =>
            val address = Base58Check.bytesToBase58(wit.getAddress.toByteArray)
            val witness = new Witness
            witness.address = address
            witness.voteCount = wit.getVoteCount
            witness.isSR = wit.getIsJobs
            witness
          }
          .sort((a, b) => b.voteCount.compareTo(a.voteCount))
          //top 127 candidates
          .take(topN)
          .concatMap { wit =>
            val request = BytesMessage.newBuilder.setValue(parseAddress(wit.address)).build
            stub
              .getBrokerageInfo(request)
              .map { info =>
                wit.voterRate = info.getNum.intValue
                wit
              }
          }
      }
      .collectList()
      .flatMapMany { list =>
        val totalVote = list.stream().mapToLong(_.voteCount).sum()
        val voteNum   = 100_0000

        val s = list.stream()
          .map { wit =>
            val blockReward: BigDecimal = {
              if (wit.isSR) BigDecimal(460800) / 27 * (100 - wit.voterRate) / 100 * voteNum / wit.voteCount
              else 0
            }
            val voteReward              = BigDecimal(4608000) * voteNum / totalVote * (100 - wit.voterRate) / 100

            wit.reward = blockReward + voteReward
            wit.apr = wit.reward * 365 / voteNum
            wit
          }
          .sorted((a, b) => b.apr.compareTo(a.apr))

        Flux.fromStream(s)
      }
  }
}
