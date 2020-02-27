import org.apache.spark.sql.SparkSession


object MainTest {

  def main(args: Array[String]): Unit = {

    //val conf : SparkConf = new SparkConf().setAppName("MainTestApp").setMaster("local[2]")

    //val spark : SparkContext = new SparkContext(conf)


    val sparkSession: SparkSession = SparkSession.builder().appName("MainTestApp")
      .master("local[2]").getOrCreate()
    //sparkSession.sparkContext.setLogLevel("ERROR")


    import sparkSession.implicits._

    println(List(1, 2, 3, 4).toDS
      .map(_ + 1)
      .filter(i => i % 2 == 0)
      .reduce(_ + _))
  }

}
