package bot

import bot.imgur.Service
import com.bot4s.telegram.models.{Message, User}

import scala.collection.mutable
import scala.concurrent.Future

class SimpleServer(val imgurService: Service) extends Server {

  val usersBase: mutable.Map[Int, User] = mutable.Map()
  val messagesBase: mutable.Map[Int, mutable.MutableList[String]] = mutable.Map().withDefaultValue(mutable.MutableList())

  override def addUser(implicit msg: Message): Unit = msg.from.foreach(user => usersBase += user.id -> user)

  override def users: String = usersBase.values.mkString

  override def sendMessage(args: Seq[String]): Unit = {
    if (args.isEmpty) throw new IndexOutOfBoundsException()
    val id = args.head.toInt
    val s = args.tail
    if (s.nonEmpty) {
      messagesBase(id) += s.mkString(" ")
    }
  }

  override def getRandomCat: Future[String] = imgurService.getRandomCat

  override def getNewMessagesTo(id: Int): List[String] = messagesBase(id).toList

  override def readNewMessagesTo(id: Int): Unit = messagesBase -= id
}