package optimization

import java.io.File

import org.apache.spark.sql.DataFrame

object ParquetOptimization {
  def apply(dataFrame: DataFrame, path: String): DataFrame = {
    // Check if the parquet files exists, and create the if not
    val file: File = new File(path)
    if (!file.exists()) {
      dataFrame.write.partitionBy("region").parquet(path)
    }

    // Load the data from parquet file
    dataFrame.sparkSession.read.parquet(path)
  }
}
