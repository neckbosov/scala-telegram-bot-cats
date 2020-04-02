package bot.db.types

case class UserMessage(messageId: Option[Int], userId: Int, text: String)