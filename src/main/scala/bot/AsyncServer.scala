package bot

import com.bot4s.telegram.models.Message

import scala.concurrent.Future

trait AsyncServer {
  def init: Future[Unit] = Future.unit

  def addUser(implicit msg: Message): Future[Unit]

  def users: Future[String]

  def sendMessage(args: Seq[String]): Future[Unit]

  def getRandomCat: Future[String]

  def getNewMessagesTo(id: Int): Future[List[String]]

  def readNewMessagesTo(id: Int): Future[Unit]
}
