package bot

import com.bot4s.telegram.models.Message

import scala.concurrent.Future

trait AsyncServer {
  def init: Future[Unit] = Future.unit

  def addUser(implicit msg: Message): Future[Unit]

  def users: Future[String]

  def sendMessage(args: Seq[String]): Future[Unit]

  def getRandomCat(implicit msg: Message): Future[String]

  def popNewMessagesTo(id: Int): Future[List[String]]

  def getStats(idOrLogin: String): Future[Option[String]]
}
