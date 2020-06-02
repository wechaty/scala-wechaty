package wechaty.puppet

import org.slf4j.LoggerFactory

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-02
  */
trait LoggerSupport {
  protected val logger = LoggerFactory getLogger getClass

  protected def info(message: String, args: Any*): Unit = {
    if (logger.isInfoEnabled) {
      logger.info(message, args.map(_.asInstanceOf[AnyRef]).toArray)
    }
  }

  protected def warn(message: String, args: Any*): Unit = {
    if (logger.isWarnEnabled) {
      logger.warn(message, args.map(_.asInstanceOf[AnyRef]).toArray)
    }
  }

  protected def error(message: String, args: Any*): Unit = {
    if (logger.isErrorEnabled) {
      logger.error(message, args.map(_.asInstanceOf[AnyRef]).toArray)
    }
  }

  protected def error(message: String, e: Throwable): Unit = {
    if (logger.isErrorEnabled) {
      logger.error(message, e)
    }
  }

  protected def debug(message: String, args: Any*): Unit = {
    if (logger.isDebugEnabled) {
      logger.debug(message, args.map(_.asInstanceOf[AnyRef]).toArray)
    }
  }
}
