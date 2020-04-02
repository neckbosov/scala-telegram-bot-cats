package bot.db.tables

import com.bot4s.telegram.models.User
import slick.jdbc.H2Profile.api._

class Users(tag: Tag) extends Table[User](tag, "USERS") {
  def id = column[Int]("id", O.PrimaryKey)

  def isBot = column[Boolean]("BOT_FLAG")

  def firstName = column[String]("FIRST_NAME")

  def lastName = column[Option[String]]("LAST_NAME", O.Default(None))

  def username = column[Option[String]]("USERNAME", O.Default(None))

  def languageCode = column[Option[String]]("LANGUAGE_CODE", O.Default(None))

  override def * = (id, isBot, firstName, lastName, username, languageCode) <> (User.tupled, User.unapply)
}

