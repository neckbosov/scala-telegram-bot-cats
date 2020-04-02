package bot

import com.bot4s.telegram.models.Message

import scala.concurrent.Future

trait Server {
  def addUser(implicit msg: Message): Unit

  def users: String

  def sendMessage(args: Seq[String]): Unit

  def getRandomCat: Future[String]

  def getNewMessagesTo(id: Int): List[String]

  def readNewMessagesTo(id: Int): Unit
}
