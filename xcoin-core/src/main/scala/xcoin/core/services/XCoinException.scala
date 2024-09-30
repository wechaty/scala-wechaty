package xcoin.core.services


abstract class XCoinException(val code:Int,message:String) extends RuntimeException(message) {
  def errorName= this match {
    case e :Product=> e.productPrefix
    case other => this.getClass.getSimpleName
  }
}

object XCoinException{
  case class XInvalidParameterException(message:String) extends XCoinException(1, message){
    def this(name:String, value:Any){
      this("invalid value [%s] for [%s]".format(value, name))
    }
  }
  case object XAccessDeniedException extends XCoinException(2, "Access Denied")
  case class XResourceNotFoundException(resourcePath:String) extends XCoinException(3, "resource [%s] not found".format(resourcePath))
  case class XFailRequestException(failMessage:String) extends XCoinException(4, "failt to request:[%s]".format(failMessage))
  case class XInvalidReturnException(failMessage:String) extends XCoinException(5, "invalid return:[%s]".format(failMessage))
  case class XInvalidStateException(failMessage:String) extends XCoinException(5, "invalid state:[%s]".format(failMessage))
  case class XUnknownException(message:String) extends XCoinException(9999, message)
}
