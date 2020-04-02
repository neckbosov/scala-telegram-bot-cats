package bot.imgur

import scala.concurrent.Future

trait Service {
  def getRandomCat: Future[String]
}
