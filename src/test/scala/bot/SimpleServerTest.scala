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

  "Server" should "send send a lot of messages" in new mocks {
    server.sendMessage(List("0", "Alya is cool!!"))
    server.sendMessage(List("1", "kak dela?"))

    server.messagesBase.size shouldBe 2
    server.messagesBase(0).mkString("") shouldBe "Alya is cool!!"
    server.messagesBase(1).mkString("") shouldBe "kak dela?"


    server.sendMessage(List("3", "poka ne rodila"))
    server.messagesBase.size shouldBe 3
    server.messagesBase(3).mkString("") shouldBe "poka ne rodila"
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
    server.sendMessage(List("0", "no interesnaya)0))0))))))00))"))

    server.readNewMessagesTo(0)
    server.sendMessage(List("0", "kanikul ne budet((((("))

    server.getNewMessagesTo(0) shouldBe List("kanikul ne budet(((((")
  }


  "Server" should "erase all read messages 2" in new mocks {
    server.getNewMessagesTo(0) shouldBe List()
    server.getNewMessagesTo(1) shouldBe List()
    server.getNewMessagesTo(2) shouldBe List()

    server.sendMessage(List("0", "niht"))
    server.sendMessage(List("0", "dja"))
    server.sendMessage(List("1", "da"))
    server.sendMessage(List("1", "net"))
    server.sendMessage(List("2", "oui"))
    server.sendMessage(List("2", "non"))

    server.getNewMessagesTo(0) shouldBe List("niht", "dja")
    server.getNewMessagesTo(1) shouldBe List("da", "net")
    server.getNewMessagesTo(2) shouldBe List("oui", "non")

    server.readNewMessagesTo(0)

    server.getNewMessagesTo(0) shouldBe List()
    server.getNewMessagesTo(1) shouldBe List("da", "net")
    server.getNewMessagesTo(2) shouldBe List("oui", "non")

    server.readNewMessagesTo(1)
    server.readNewMessagesTo(2)

    server.getNewMessagesTo(0) shouldBe List()
    server.getNewMessagesTo(1) shouldBe List()
    server.getNewMessagesTo(2) shouldBe List()
  }

}
