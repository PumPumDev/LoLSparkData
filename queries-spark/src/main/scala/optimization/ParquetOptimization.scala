package optimization

import org.apache.spark.sql.{DataFrame, SaveMode}

object ParquetOptimization {
  def apply(dataFrame: DataFrame, path: String): DataFrame = {
    //Write the data if does not exist
    dataFrame.write.mode(SaveMode.Ignore).partitionBy("region").parquet(path)
    // Load the data from parquet file
    dataFrame.sparkSession.read.parquet(path)
  }
}
