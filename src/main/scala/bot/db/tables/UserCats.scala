package bot.db.tables

import bot.db.types.UserCat
import slick.jdbc.H2Profile.api._

class UserCats(tag: Tag) extends Table[UserCat](tag, "user_cats") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

  def userId = column[Int]("user_id")

  def catLink = column[String]("cat_link")

  override def * = (id.?, userId, catLink) <> (UserCat.tupled, UserCat.unapply)
}
