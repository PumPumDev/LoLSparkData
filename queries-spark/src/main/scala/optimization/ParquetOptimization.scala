package optimization

import java.io.File

import org.apache.spark.sql.{DataFrame, SparkSession}

object ParquetOptimization {
  def apply(spark: SparkSession, dataFrame: DataFrame, path: String): DataFrame = {
    // Check if the parquet files exists, and create the if not
    val file: File = new File(path)
    if (!file.exists()) {
      dataFrame.write.parquet(path)
    }

    // Load the data from parquet file
    spark.read.parquet(path)
  }
}
