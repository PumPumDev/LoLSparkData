package queries

import org.apache.spark.sql.DataFrame
import utils.TimeMeasure

case class CompareQueries(function: DataFrame => DataFrame, nonOptDF: DataFrame,
                          optDF: DataFrame, funName: String) {
  def compareDS
  : CompareQueriesResult = {
    val nonOptTime = TimeMeasure(this.function(this.nonOptDF).limit(20).collect()) / 1E9
    val optTime = TimeMeasure(this.function(this.optDF).limit(20).collect()) / 1E9
    CompareQueriesResult(this.funName, nonOptTime, optTime)
  }
}
