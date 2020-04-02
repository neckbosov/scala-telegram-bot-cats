package bot.imgur.random

import scala.util.Random

object RandomRest extends Randomize {
  override def randomElem[T](ls: List[T]): T = Random.shuffle(ls).head
}
