package bot

import bot.imgur.Service
import com.bot4s.telegram.models.{Message, User}
import slick.jdbc.H2Profile.api._

import scala.concurrent.{ExecutionContext, Future}


class Users(tag: Tag) extends Table[User](tag, "USERS") {
  def id = column[Int]("id", O.PrimaryKey)

  def isBot = column[Boolean]("BOT_FLAG")

  def firstName = column[String]("FIRST_NAME")

  def lastName = column[Option[String]]("LAST_NAME", O.Default(None))

  def username = column[Option[String]]("USERNAME", O.Default(None))

  def languageCode = column[Option[String]]("LANGUAGE_CODE", O.Default(None))

  override def * = (id, isBot, firstName, lastName, username, languageCode) <> (User.tupled, User.unapply)
}

case class UserMessage(messageId: Option[Int], userId: Int, text: String)

class Messages(tag: Tag) extends Table[UserMessage](tag, "messages") {
  def messageId = column[Int]("message_id", O.PrimaryKey, O.AutoInc)

  def userId = column[Int]("user_id")

  def text = column[String]("text")

  def * = (messageId.?, userId, text) <> (UserMessage.tupled, UserMessage.unapply)
}

class DbServer(val imgurService: Service, val db: Database)(implicit val ec: ExecutionContext) extends AsyncServer {
  private val usersBase = TableQuery[Users]
  private val messagesBase = TableQuery[Messages]

  override def init: Future[Unit] = {
    val transaction = for {
      _ <- usersBase.schema.createIfNotExists
      _ <- messagesBase.schema.createIfNotExists
    } yield ()
    db.run(transaction)
  }

  override def addUser(implicit msg: Message): Future[Unit] = {
    println("called addUser")
    msg.from match {
      case Some(user) => db.run(usersBase.insertOrUpdate(user)).map(_ => ())
      case None => Future.unit
    }
  }

  override def users: Future[String] = db.run(usersBase.result).map(_.mkString)

  override def sendMessage(args: Seq[String]): Future[Unit] = {
    if (args.isEmpty) throw new IndexOutOfBoundsException()
    val id = args.head.toInt
    val s = args.tail
    if (s.nonEmpty) {
      db.run(messagesBase += UserMessage(None, id, s.mkString(" "))).map(_ => ())
    } else {
      Future.unit
    }
  }

  override def getRandomCat: Future[String] = imgurService.getRandomCat

  override def popNewMessagesTo(id: Int): Future[List[String]] = {
    val transaction = for {
      res <- messagesBase.filter(_.userId === id).map(_.text).result
      _ <- messagesBase.filter(_.userId === id).delete
    } yield res.toList
    db.run(transaction)
  }
}
