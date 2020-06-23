package wechaty.plugins

import java.util.concurrent.TimeUnit

import com.typesafe.scalalogging.LazyLogging
import wechaty.Wechaty.PuppetResolver
import wechaty.user.Room

/**
  *
  * @author <a href="mailto:jcai@ganshane.com">Jun Tsai</a>
  * @since 2020-06-23
  */
object PluginHelper extends LazyLogging {
  private[plugins] def findRooms(roomIds: Array[String])(implicit resolver: PuppetResolver): Array[Room] = {
    try {
      roomIds.flatMap(Room.load(_)) //avoid timeout ?
    } catch {
      case e: Throwable =>
        logger.warn("load room occurs exception,so loop load", e)
        Thread.sleep(TimeUnit.SECONDS.toMillis(5)) //sleep 5s to wait
        findRooms(roomIds)
    }
  }

}
