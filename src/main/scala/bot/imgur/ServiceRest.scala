package bot.imgur

import bot.imgur.random.{RandomRest, Randomize}
import com.softwaremill.sttp.json4s._
import com.softwaremill.sttp.{SttpBackend, sttp, _}
import org.json4s.native.Serialization

import scala.concurrent.{ExecutionContext, Future}

class ServiceRest(val apikey: String, val rng: Randomize = RandomRest)(
  implicit val backend: SttpBackend[Future, Nothing],
  implicit val ec: ExecutionContext,
  implicit val serialization: Serialization.type = org.json4s.native.Serialization) extends Service {
  override def getRandomCat: Future[String] = {
    val request = sttp
      .header("Authorization", s"Client-ID $apikey")
      .get(uri"https://api.imgur.com/3/gallery/search?q=cats")
      .response(asJson[api.Response])

    backend.send(request).map { response =>
      val u = response.unsafeBody
      val images = u.data.flatMap(_.images)
      rng.randomElem(images).link
    }
  }
}
