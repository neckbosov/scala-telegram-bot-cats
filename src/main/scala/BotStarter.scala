import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.{FutureSttpClient, ScalajHttpClient}
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.bot4s.telegram.models.User
import com.softwaremill.sttp.SttpBackendOptions
import com.softwaremill.sttp.okhttp.OkHttpFutureBackend
import slogging.{LogLevel, LoggerConfig, PrintLoggerFactory}

import scala.concurrent.duration.Duration
import scala.util.Try
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.collection.mutable

class BotStarter(override val client: RequestHandler[Future]) extends TelegramBot
  with Polling
  with Commands[Future] {
  val users: mutable.Map[Int, User] = mutable.Map()
  val messages: mutable.Map[Int, mutable.MutableList[String]] = mutable.Map().withDefaultValue(mutable.MutableList())
  onCommand("/start") { implicit msg =>
    msg.from match {
      case Some(user) => {
        users += (user.id -> user)
        Future.unit
      }
      case None => Future.unit
    }
  }

  onCommand("/users") { implicit msg =>
    val res = users.values.mkString
    reply(res).void
  }

  onCommand("/send") { implicit msg =>
    withArgs { args =>
      try {
        if (args.isEmpty) throw new IndexOutOfBoundsException()
        val id = args.head.toInt
        val s = args.tail
        if (s.nonEmpty) {
          messages(id) += s.mkString(" ")
        }
        Future.unit
      } catch {
        case _: NumberFormatException => reply("Invalid argument. Usage: /send id message").void
        case _: IndexOutOfBoundsException => reply("Empty argument list. Usage: /send id message").void
      }
    }
  }

  onCommand("/check") { implicit msg =>
    msg.from.map(_.id) match {
      case Some(id) => {
        val res = messages(id)
        if (res.isEmpty) {
          reply("I have no new messages for you...").void
        } else {
          reply("You new messages:").void
          res.foreach(reply(_).void)
          messages -= id
          Future.unit
        }
      }
      case None => Future.unit
    }
  }
}

object BotStarter {
  def main(args: Array[String]): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.global
    implicit val backend = OkHttpFutureBackend(
      SttpBackendOptions.Default.socksProxy("ps8yglk.ddns.net", 11999)
    )
    val source = scala.io.Source.fromFile("apikey.txt")
    val token = try source.mkString finally source.close()
    val bot = new BotStarter(new FutureSttpClient(token))
    Await.result(bot.run(), Duration.Inf)
  }
}
