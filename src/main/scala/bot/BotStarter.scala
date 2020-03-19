package bot

import bot.imgur.ServiceRest
import cats.instances.future._
import cats.syntax.functor._
import com.bot4s.telegram.api.RequestHandler
import com.bot4s.telegram.api.declarative.Commands
import com.bot4s.telegram.clients.FutureSttpClient
import com.bot4s.telegram.future.{Polling, TelegramBot}
import com.softwaremill.sttp.okhttp.OkHttpFutureBackend
import com.softwaremill.sttp.{SttpBackend, SttpBackendOptions}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class BotStarter(override val client: RequestHandler[Future], val server: Server) extends TelegramBot
  with Polling
  with Commands[Future] {
  onCommand("/start") { implicit msg =>
    server.addUser
    reply("Cats-bot started").void
  }

  onCommand("/users") { implicit msg =>
    reply(server.users).void
  }

  onCommand("/send") { implicit msg =>
    withArgs { args =>
      try {
        server.sendMessage(args)
        Future.unit
      } catch {
        case _: NumberFormatException => reply("Invalid argument. Usage: /send id message").void
        case _: IndexOutOfBoundsException => reply("Empty argument list. Usage: /send id message").void
      }
    }
  }

  onCommand("/check") { implicit msg =>
    msg.from.map(_.id) match {
      case Some(id) =>
        val res = server.getNewMessagesTo(id)
        if (res.isEmpty) {
          reply("I have no new messages for you...").void
        } else {
          reply("You new messages:").void
          res.foreach(reply(_).void)
          server.readNewMessagesTo(id)
          Future.unit
        }

      case None => Future.unit
    }
  }

  onCommand("/cat") { implicit msg =>
    server.getRandomCat.flatMap(link => reply(link)).void
  }
}

object BotStarter {
  def main(args: Array[String]): Unit = {
    implicit val ec: ExecutionContext = ExecutionContext.global
    implicit val backend: SttpBackend[Future, Nothing] = OkHttpFutureBackend(
      SttpBackendOptions.Default.socksProxy("ps8yglk.ddns.net", 11999)
    )
    val source = scala.io.Source.fromFile("apikey.txt")
    val apiIter = source.getLines()
    val token = apiIter.next()
    val imgurApiKey = apiIter.next()
    val bot = new BotStarter(new FutureSttpClient(token), new SimpleServer(new ServiceRest(imgurApiKey)))
    source.close()
    Await.result(bot.run(), Duration.Inf)
  }
}
