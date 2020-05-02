package utils

object TimeMeasure {
  def apply[T](f: => T): (T, Long) = {
    val start = System.nanoTime()
    val res = f
    (res, System.nanoTime() - start)
  }
}
