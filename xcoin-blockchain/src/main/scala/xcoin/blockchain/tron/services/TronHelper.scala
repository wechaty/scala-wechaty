package xcoin.blockchain.tron.services

import com.typesafe.scalalogging.LazyLogging
import org.tron.trident.utils.Base58Check

import scala.util.Try
import scala.util.Success
import scala.util.Failure


object TronHelper extends LazyLogging{
  def isValidAddress(address: String): Boolean = {
    Try {
      Base58Check.base58ToBytes(address)
    } match {
      case Success(_) => true
      case Failure(exception) =>
        logger.warn("invalid address {} ,e:{}", address, exception.toString)
        false
    }
  }

}
