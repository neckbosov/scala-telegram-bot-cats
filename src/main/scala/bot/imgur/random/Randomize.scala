package bot.imgur.random

trait Randomize {
  def randomElem[T](ls: List[T]): T
}
