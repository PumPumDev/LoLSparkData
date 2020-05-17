package measure

object TimeMeasure {
  def apply[T](f: => T): Long = {
    val start = System.nanoTime()
    val _ = f
    System.nanoTime() - start
  }
}
