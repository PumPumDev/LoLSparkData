import configuration.Configuration.outputPath
import dto.RegionDTO
import dto.`match`.MatchDTO
import dto.player.LeagueListDTO
import optimization.ParquetOptimization
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Encoders, SparkSession}
import paths.ModelDataPaths.{getMatchesPath, getPlayerPath}
import queries.BasicSparkQueries._
import queries.CompareQueries
import treatment.DataTreatment._
import treatment._
import configuration.Configuration.AWSLaunch

object SparkQueriesMain extends App {

  //WIP
  implicit val spark: SparkSession =
    if (AWSLaunch) SparkSession.builder().appName("Spark Queries")
    .getOrCreate()
    else
      SparkSession.builder().appName("Spark Queries")
        .master("local[*]")
        .getOrCreate()

  import spark.implicits._

  //Download the constant info champions, etc...??

  val parquetPath: String = s"$outputPath/parquet"

  val playersDF: DataFrame = RegionDTO.getAllRegions.map(reg => spark.read.format("json")
    .option("sep", "\n").schema(Encoders.product[LeagueListDTO].schema).load(getPlayerPath(outputPath, reg))
    .withColumn("region", lit(reg.toString)).select($"region", explode($"entries"))
    .select($"region", $"col.*")).reduce[DataFrame](_.union(_))

  val matchesDF: DataFrame = RegionDTO.getAllRegions.map(reg => spark.read.format("json")
    .option("sep", "\n").schema(Encoders.product[MatchDTO].schema).load(getMatchesPath(outputPath, reg))
    .withColumn("region", lit(reg.toString))).reduce[DataFrame](_.union(_))

  val playerStatsDF = playerStats(playersDF, allStats(matchesDF))

  val optimizedPlayersDF = ParquetOptimization(playersDF, s"$parquetPath/players")

  val optimizedMatchesDF = ParquetOptimization(matchesDF, s"$parquetPath/matches")

  val optimizedPlayerStatsDF = ParquetOptimization(playerStatsDF, s"$parquetPath/player_stats")


  // Compare dataframes with and without optimization
  val comparableQueries: List[CompareQueries] =
    List(
      CompareQueries(winRatePerPlayer, playersDF, optimizedPlayersDF, "Win Rate per Player"),
      CompareQueries(rankedClassicGames, matchesDF, optimizedMatchesDF, "Ranked Classic Games"),
      CompareQueries(firstBloodParticipant, playerStatsDF, optimizedPlayerStatsDF, "First Blood Participant")
    )

  comparableQueries.map(_.compareDS).foreach(_.show)


  // Response questions about data
  val rankPlayers = rankingPlayers(optimizedPlayersDF)

  cleanVisualData()


  // Creation of the graphs
  tablesPerRegion("Jugadores", optimizedPlayersDF)
  tablesPerRegion("WinRate", winRatePerPlayer(rankingPlayers(optimizedPlayersDF)))


  graphPerRegion[Int,Double]("Win Rate Vs Position", winRatePerPlayer(rankPlayers), $"top", $"winRate", Int_Double)
  graphPerRegion[Int,Int]("Number Matches Vs Position", totalMatchesPerPlayer(rankPlayers), $"top", $"totalMatches", Int_Int)
  graphPerRegion[String, Double]("Region Avg Number Matches", averageMatches(optimizedPlayersDF),$"region",$"average_matches", String_Double)
  graphPerRegion[String,Double]("Region Avg Time Per Match", averageMatchTimePerRegion(optimizedMatchesDF), $"region",$"Average Duration (min)", String_Double)

  graphPerRegion[Int,Long]("First Blood Participation Vs Position", firstBloodParticipant(optimizedPlayerStatsDF),$"top",$"First blood participant", Int_Long)

  graphPerRegion[Int,Double]("KDA Vs Position", kdaPlayers(optimizedPlayerStatsDF),$"top", $"KDA", Int_Double)

  graphPerRegion[Int,Double]("Gold Earned vs Position", averageGoldEarned(optimizedPlayerStatsDF), $"top", $"goldEarned", Int_Double)

  graphPerRegion[Int,Double]("Dmg deal Vs Position", averageDmgDeal(optimizedPlayerStatsDF), $"top", $"damageDeal", Int_Double)

  graphPerRegion[Int,Double]("Minions killed Vs Position", averageMinionsKilled(optimizedPlayerStatsDF), $"top", $"minionsKilled", Int_Double)

  graphPerRegion[Int,Double]("Total heal Vs Position", averageTotalHeal(optimizedPlayerStatsDF), $"top", $"totalHeal", Int_Double)
  graphPerRegion[Int,Double]("Vision Score Vs Position", averageVisionScore(optimizedPlayerStatsDF), $"top", $"visionScore", Int_Double)

  graphPerRegion[Int,Double]("Neutral Minions Killed Vs Position", averageNeutralMinionsKilled(optimizedPlayerStatsDF), $"top", $"neutralMinionsKilled", Int_Double)

  graphPerRegion[Int,Double]("Time CCing Vs Position", averageCCtime(optimizedPlayerStatsDF), $"top", $"timeCCing", Int_Double)

  spark.stop()

  println("Data is ready to display")

}
