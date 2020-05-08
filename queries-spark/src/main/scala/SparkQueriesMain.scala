import configuration.Configuration._
import dto.RegionDTO
import dto.`match`.MatchDto
import dto.player.LeagueListDTO
import optimization.ParquetOptimization
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Encoders, SparkSession}
import paths.ModelDataPaths.{getMatchesPath, getPlayerPath}
import queries.BasicSparkQueries._
import utils.TimeMeasure

object SparkQueriesMain extends App {

  //WIP
  val spark: SparkSession = SparkSession.builder().appName("Spark Queries")
    .master("local[*]").getOrCreate()
  import spark.implicits._

  //Download the constant info champions, etc...??

  val parquetPath: String = s"$outputPath/parquet"

  val playersData: DataFrame = RegionDTO.getAllRegions.map(reg => spark.read.format("json")
    .option("sep", "\n").schema(Encoders.product[LeagueListDTO].schema).load(getPlayerPath(outputPath, reg))
    .withColumn("region", lit(reg.toString)).select($"region", explode($"entries"))
    .select($"region", $"col.*")).reduce[DataFrame](_.union(_))

  val matchesData: DataFrame = RegionDTO.getAllRegions.map(reg => spark.read.format("json")
    .option("sep", "\n").schema(Encoders.product[MatchDto].schema).load(getMatchesPath(outputPath, reg))
    .withColumn("region", lit(reg.toString))).reduce[DataFrame](_.union(_))


  val playerStatsDF = playerStats(spark, playersData, allStats(spark, matchesData))

  val optimizedPlayers = ParquetOptimization(spark, playersData, s"$parquetPath/players")

  val optimizedMatches = ParquetOptimization(spark, matchesData, s"$parquetPath/matches")

  val optimizedPlayerStats = ParquetOptimization(spark, playerStatsDF, s"$parquetPath/player_stats")

  println("Non optimized players " + TimeMeasure(playersData show)._2 / 1E9)

  println("Optimized players " + TimeMeasure(optimizedPlayers show)._2 / 1E9)


  // Use the queries and see the performance with TimeMeasure object

}
