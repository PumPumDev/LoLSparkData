package queries

case class CompareQueriesResult(funName: String, nonOptTime: Double, optTime: Double){
  def show : Unit = {
    println(s"$funName\nNon Optimized Time: $nonOptTime\nOptimized Time: $optTime\n")
  }
}
