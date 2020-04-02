package bot.db.tables

import bot.db.types.UserMessage
import slick.jdbc.H2Profile.api._

class Messages(tag: Tag) extends Table[UserMessage](tag, "messages") {
  def messageId = column[Int]("message_id", O.PrimaryKey, O.AutoInc)

  def userId = column[Int]("user_id")

  def text = column[String]("text")

  override def * = (messageId.?, userId, text) <> (UserMessage.tupled, UserMessage.unapply)
}
