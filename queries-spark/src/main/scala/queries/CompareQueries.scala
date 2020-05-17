package queries

import org.apache.spark.sql.DataFrame

case class CompareQueries(function: DataFrame => DataFrame, nonOptDF: DataFrame,
                          optDF: DataFrame, funName: String)
