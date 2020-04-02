package bot

import java.util.NoSuchElementException

import bot.imgur.api.{Data, InnerData}
import bot.imgur.random.Randomize
import bot.imgur.{Service, ServiceRest, api}
import com.softwaremill.sttp.{Response, StatusCode, SttpBackend}
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.control.Exception


class RandomizerStub extends Randomize {
  override def randomElem[T](ls: List[T]): T = ls.head
}


class ServiceTest extends AnyFlatSpec with Matchers with MockFactory {
  trait mocks {
    implicit val ec = ExecutionContext.global
    implicit val backend = mock[SttpBackend[Future, Nothing]]

    val service = new ServiceRest("", new RandomizerStub())
  }

  "ServiceRest" should "throw NoCatException" in new mocks {
    (backend.send[String] _).expects(*).returning(Future.successful(
        Response.error("no cats: 404", 404)
    ))

    ScalaFutures.whenReady(service.getRandomCat.failed) { e =>
      e shouldBe an[NoSuchElementException]
    }
  }


  "ServiceRest" should "return single cat" in new mocks {
    (backend.send[api.Response] _).expects(*).returning(Future.successful(
      Response.ok(new imgur.api.Response(List(Data(images = List(InnerData("single cat"))))))
    ))

    ScalaFutures.whenReady(service.getRandomCat) { cat =>
      cat shouldBe "single cat"
    }
  }


  "ServiceRest" should "return first cat" in new mocks {
    (backend.send[api.Response] _).expects(*).returning(Future.successful(
      Response.ok(new imgur.api.Response(List(Data(images = List(
        InnerData("first cat"),
        InnerData("second cat"),
        InnerData("third cat"))))))
    ))

    ScalaFutures.whenReady(service.getRandomCat) { cat =>
      cat shouldBe "first cat"
    }
  }
}
