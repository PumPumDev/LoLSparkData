import dto.`match`.MatchDto
import org.apache.spark.sql.{Encoders, SparkSession}

object SparkQueriesMain extends App {
  //WIP
  val spark: SparkSession = SparkSession.builder().appName("Spark Queries")
    .master("local[*]").getOrCreate()

  import spark.implicits._

  val schema = Encoders.product[MatchDto].schema
  schema.printTreeString()
  val read = spark.read.format("json").option("sep", "\n").schema(schema)
    .load("api_data/matches/br1.data").as[MatchDto]
  read.show

}
