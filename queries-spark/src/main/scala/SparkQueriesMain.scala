import dto.RegionDTO
import dto.`match`.MatchDTO
import dto.player.LeagueListDTO
import optimization.ParquetOptimization
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Encoders, SparkSession}
import paths.ModelDataPaths.{getMatchesPath, getPlayerPath}
import queries.BasicSparkQueries._
import measure.TimeMeasure
import queries.{CompareQueries, CompareQueriesResult}

object SparkQueriesMain extends App {

  //WIP
  implicit val spark: SparkSession = SparkSession.builder().appName("Spark Queries")
    .master("local[*]").getOrCreate()
  import spark.implicits._

  //Download the constant info champions, etc...??

  val parquetPath: String = "s3://league-data/parquet"

  val playersDF: DataFrame = RegionDTO.getAllRegions.map(reg => spark.read.format("json")
    .option("sep", "\n").schema(Encoders.product[LeagueListDTO].schema).load(getPlayerPath("s3://league-data", reg))
    .withColumn("region", lit(reg.toString)).select($"region", explode($"entries"))
    .select($"region", $"col.*")).reduce[DataFrame](_.union(_))

  val matchesDF: DataFrame = RegionDTO.getAllRegions.map(reg => spark.read.format("json")
    .option("sep", "\n").schema(Encoders.product[MatchDTO].schema).load(getMatchesPath("s3://league-data", reg))
    .withColumn("region", lit(reg.toString))).reduce[DataFrame](_.union(_))

  val playerStatsDF = playerStats(playersDF, allStats(matchesDF))

  val optimizedPlayersDF = ParquetOptimization(playersDF, s"$parquetPath/players")

  val optimizedMatchesDF = ParquetOptimization(matchesDF, s"$parquetPath/matches")

  val optimizedPlayerStatsDF = ParquetOptimization(playerStatsDF, s"$parquetPath/player_stats")

  // Compare optimized and non optimized DataFrames at performing operation
  def compareDS(compQ: CompareQueries)
  : CompareQueriesResult = {
    val nonOptTime = TimeMeasure(compQ.function(compQ.nonOptDF).limit(20).collect()) / 1E9
    val optTime = TimeMeasure(compQ.function(compQ.optDF).limit(20).collect()) / 1E9
    CompareQueriesResult(compQ.funName, nonOptTime, optTime)
  }

  val comparableQueries: List[CompareQueries] =
    List(CompareQueries(rankingPlayers, playersDF, optimizedPlayersDF, "Players Ranking"),
      CompareQueries(totalMatchesPerPlayer, playersDF, optimizedPlayersDF, "Total Matches per Player"),
      CompareQueries(winRatePerPlayer, playersDF, optimizedPlayersDF, "Win Rate per Player"),
      CompareQueries(rankedClassicGames, matchesDF, optimizedMatchesDF, "Ranked Classic Games"),
      CompareQueries(visionScorePerPlayer, matchesDF, optimizedMatchesDF, "Vision Score per Player"),
      CompareQueries(firstBloodParticipant, playerStatsDF, optimizedPlayerStatsDF, "First Blood Participant"))

  println(comparableQueries.map(compareDS))

  // Response questions about data
  val firstBloodParticipantDF: DataFrame = firstBloodParticipant(optimizedPlayerStatsDF)


  spark.stop()

}
