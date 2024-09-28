package xcoin.core.services

import xcoin.core.services.XCoinException.GenericError

class XCoinException(val error: GenericError ) extends RuntimeException(error.message) {
  def errorName= error match {
    case e :Product=> e.productPrefix
    case other => error.getClass.getSimpleName
  }
}

object XCoinException{
  abstract class GenericError(code:Int,val message:String)

  case class InvalidParameter(name:String,value:Any) extends GenericError(1,"invalid value [%s] for [%s]".format(value,name))
  case object AccessDenied extends GenericError(2,"Access Denied")
  case class ResourceNotFound(resourcePath:String) extends GenericError(3,"resource [%s] not found".format(resourcePath))
  case class FailRequest(failMessage:String) extends GenericError(4,"failt to request:[%s]".format(failMessage))
  case class InvalidReturn(failMessage:String) extends GenericError(5,"invalid return:[%s]".format(failMessage))
  case class Unknown(override val message:String) extends GenericError(9999, message)
}
