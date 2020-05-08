package queries

import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

object BasicSparkQueries {

  // Players Queries
  def rankingPlayers(spark: SparkSession, players: DataFrame): DataFrame = {
    import spark.implicits._
    players select($"*", rank over Window.partitionBy($"region").orderBy($"leaguePoints".desc) alias "top") orderBy($"region".asc, $"leaguePoints".desc)
  }

  def topPlayerAveragePoints(spark: SparkSession, players: DataFrame): DataFrame = {
    import spark.implicits._
    rankingPlayers(spark, players) groupBy $"region" agg avg("leaguePoints") withColumnRenamed("avg(leaguePoints)", "Average Points")
  }

  def topNPlayer(spark: SparkSession, players: DataFrame, n: Int): DataFrame = {
    import spark.implicits._
    players select($"*", rank over Window.partitionBy($"region").orderBy($"leaguePoints".desc) alias "top") filter ($"rank" <= n) orderBy($"region".asc, $"leaguePoints".desc)
  }

  def topNPlayerAveragePoints(spark: SparkSession, players: DataFrame, n: Int): DataFrame = {
    import spark.implicits._
    topNPlayer(spark, players, n) groupBy $"region" agg avg("leaguePoints") withColumnRenamed("avg(leaguePoints)", "Average Points")
  }

  def totalMatchesPerPlayer(spark: SparkSession, players: DataFrame): DataFrame = {
    import spark.implicits._
    players orderBy($"region".asc, $"leaguePoints".desc) withColumn("totalMatches", $"wins" + $"losses")
  }

  def averageMatches(spark: SparkSession, players: DataFrame): DataFrame = {
    import spark.implicits._
    totalMatchesPerPlayer(spark, players) groupBy $"region" agg avg($"totalMatches")
  }

  def winRatePerPlayer(spark: SparkSession, players: DataFrame): DataFrame = {
    import spark.implicits._
    players orderBy($"region".asc, $"leaguePoints".desc) withColumn("winRate", ($"wins" / ($"losses" + $"wins")) * 100)
  }

  def averageWinRate(spark: SparkSession, players: DataFrame): DataFrame = {
    import spark.implicits._
    winRatePerPlayer(spark, players) groupBy $"region" agg avg($"winRate") withColumnRenamed("avg(winRate)", "Average Win Rate")
  }

  // Matches Queries
  def rankedClassicGames(spark: SparkSession, matches: DataFrame): DataFrame = {
    import spark.implicits._
    matches filter $"gameMode" === "CLASSIC" && $"gameType" === "MATCHED_GAME"
  }

  def averageMatchTime(spark: SparkSession, matches: DataFrame): DataFrame = {
    import spark.implicits._
    matches groupBy $"region" agg avg($"gameDuration") withColumn("avg(gameDuration)", $"avg(gameDuration)" / 60) withColumnRenamed("avg(gameDuration)", "Average Duration (min)")
  }

  def visionScorePerPlayer(spark: SparkSession, matches: DataFrame): DataFrame = {
    import spark.implicits._
    matches orderBy $"region" withColumn("visionScore", $"participants.stats.visionScore")
  }

  def averageVisionScore(spark: SparkSession, matches: DataFrame): DataFrame = {
    import spark.implicits._
    val getAverageVision = udf((totalVision: Seq[Long]) => totalVision.sum[Long] / totalVision.size)
    visionScorePerPlayer(spark, matches) withColumn("Average Vision Score", getAverageVision($"visionScore"))
  }

  def allStats(spark: SparkSession, matches: DataFrame): DataFrame = {
    import spark.implicits._
    matches.orderBy($"region".desc).withColumn("identities", explode($"participantIdentities")).alias("ident")
      .join(matches.withColumn("newParticipants", explode($"participants")).as("st"), $"identities.participantId" === $"st.newParticipants.participantId"
        && $"ident.gameId" === $"st.gameId")
      .select($"ident.region", $"ident.gameId", $"identities.player" as "player", $"newParticipants.stats" as "stats", $"identities.participantId")

  }

  def playerStats(spark: SparkSession, players: DataFrame, stats: DataFrame): DataFrame = {
    import spark.implicits._

    rankingPlayers(spark, players).orderBy($"region".desc).as("pl").join(stats, $"pl.summonerName" === $"player.summonerName" && $"pl.region" === $"ident.region")
      .select($"pl.region", $"gameId", $"summonerName", $"wins", $"losses", $"stats", $"top")
  }

  def firstBloodParticipant(spark: SparkSession, playerStats: DataFrame): DataFrame = {
    import spark.implicits._
    (playerStats groupBy($"region", $"summonerName", $"top") agg count($"stats.firstBloodAssist" === true || $"stats.firstBloodKill" === true)
      withColumnRenamed("count(((stats.firstBloodAssist = true) OR (stats.firstBloodKill = true)))", "First blood participant")
      orderBy($"region".asc, $"First blood participant".desc))
  }
}
