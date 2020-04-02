package bot

import bot.db.tables.{Messages, UserCats, Users}
import bot.db.types.{UserCat, UserMessage}
import bot.imgur.Service
import com.bot4s.telegram.models.Message
import slick.jdbc.H2Profile.api._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class DbServer(val db: Database)(implicit val imgurService: Service, implicit val ec: ExecutionContext) extends AsyncServer {
  private val usersBase = TableQuery[Users]
  private val messagesBase = TableQuery[Messages]
  private val userCatsBase = TableQuery[UserCats]

  override def init: Future[Unit] = {
    val transaction = for {
      _ <- usersBase.schema.createIfNotExists
      _ <- messagesBase.schema.createIfNotExists
      _ <- userCatsBase.schema.createIfNotExists
    } yield ()
    db.run(transaction)
  }

  override def addUser(implicit msg: Message): Future[Unit] = {
    msg.from match {
      case Some(user) => db.run(usersBase += user).map(_ => ())
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

  override def getRandomCat(implicit msg: Message): Future[String] = imgurService.getRandomCat.flatMap { cat =>
    msg.from.map { user =>
      db.run(userCatsBase += UserCat(None, user.id, cat))
    }
    Future.successful(cat)
  }

  override def popNewMessagesTo(id: Int): Future[List[String]] = {
    val transaction = for {
      res <- messagesBase.filter(_.userId === id).map(_.text).result
      _ <- messagesBase.filter(_.userId === id).delete
    } yield res.toList
    db.run(transaction)
  }

  override def getStats(idOrLogin: String): Future[Option[String]] = {
    val criteriaId = Try(idOrLogin.toInt).toOption
    val transaction = for {
      realId <- usersBase.filter {
        user => user.id.? === criteriaId || user.firstName === idOrLogin
      }.map(_.id).result.headOption
      cats <- userCatsBase.filter(_.userId.? === realId).map(_.catLink).result
    } yield realId.map(_ => cats.toList)
    db.run(transaction).map(_.map(cats => cats.mkString("\n")))
  }
}
