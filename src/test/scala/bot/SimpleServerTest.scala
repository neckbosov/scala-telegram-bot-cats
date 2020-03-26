package bot

import java.util.NoSuchElementException

import bot.imgur.api.{Data, InnerData}
import bot.imgur.random.Randomize
import bot.imgur.{Service, ServiceRest, api}
import com.bot4s.telegram.clients.FutureSttpClient
import com.bot4s.telegram.models.{Chat, ChatType, Message, User}
import com.softwaremill.sttp.{Response, StatusCode, SttpBackend}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.control.Exception


class SimpleServerTest extends AnyFlatSpec with Matchers with MockFactory {
  trait mocks {
    implicit val service = mock[Service]

    val server = new SimpleServer()
  }

  "Server" should "add User" in new mocks {
    implicit val msg = new Message(0, Option(User(0, false, "priv")), 0, Chat(0, ChatType(0)))
    server.addUser
    server.users shouldBe "User(0,false,priv,None,None,None)"
  }

  "Server" should "send message" in new mocks {
    server.sendMessage(List("0", "Alya is cool!!"))
    server.messagesBase.size shouldBe 1
    server.messagesBase(0).mkString("") shouldBe "Alya is cool!!"
  }

  "Server" should "return dog, lol " in new mocks {
    (service.getRandomCat _).expects().returning(Future.successful("dog"))
    Await.result(server.getRandomCat, Duration.Inf) shouldBe "dog"
  }

  "Server" should "return new messages " in new mocks {
    server.sendMessage(List("0", "niht"))
    server.sendMessage(List("0", "dja"))

    server.getNewMessagesTo(0) shouldBe List("niht", "dja")
  }

  "Server" should "erase all read messages " in new mocks {
    server.sendMessage(List("0", "scala slozhnaya"))
    server.readNewMessagesTo(0)
    server.sendMessage(List("0", "kanikul ne budet((((("))

    server.getNewMessagesTo(0) shouldBe List("kanikul ne budet(((((")
  }
}
