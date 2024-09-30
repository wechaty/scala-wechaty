package xcoin.core.services

import org.springframework.boot.Banner
import org.springframework.boot.ansi.{AnsiColor, AnsiOutput, AnsiStyle}
import org.springframework.core.env.Environment

import java.io.PrintStream
import java.util.Properties
import scala.util.Using

object XCoinBanner extends Banner {
  private val BANNER: Array[String] = {
    """    _   _________  ________   _________   ________________  ______  __
      |   / | / / ____/ |/ /_  __/  / ____/   | / ____/_  __/ __ \/ __ \ \/ /
      |  /  |/ / __/  |   / / /    / /_  / /| |/ /     / / / / / / /_/ /\  /
      | / /|  / /___ /   | / /    / __/ / ___ / /___  / / / /_/ / _, _/ / /
      |/_/ |_/_____//_/|_|/_/    /_/   /_/  |_\____/ /_/  \____/_/ |_| /_/
      |""".stripMargin.split("\n")
  }

  private val SPRING_BOOT: String = ":: NextFactory Boot ::"

  private val STRAP_LINE_SIZE: Int = 51

  private val properties = {
    val p = new Properties()
    Using.resource(getClass.getResourceAsStream("/version.properties")){is=>
      p.load(is)
    }
    p
  }

  override def printBanner(environment: Environment, sourceClass: Class[_], printStream: PrintStream): Unit = {
    for (line <- BANNER) {
      printStream.println(line)
    }
    var version = properties.getProperty("buildId")
    version = if (version != null) " (v" + version + ")"
    else ""
    val padding = new StringBuilder
    while ( {
      padding.length < STRAP_LINE_SIZE - (version.length + SPRING_BOOT.length)
    }) padding.append(" ")
    printStream.println(AnsiOutput.toString(AnsiColor.GREEN, SPRING_BOOT, AnsiColor.DEFAULT, padding.toString, AnsiStyle.FAINT, version))
    printStream.println()
  }

}
