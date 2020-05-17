package queries

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._

object BasicSparkQueries {

  // Players Queries
  def rankingPlayers(players: DataFrame): DataFrame = {
    import players.sparkSession.implicits._
    players select($"*", rank over Window.partitionBy($"region").orderBy($"leaguePoints".desc) alias "top") orderBy($"region".asc, $"leaguePoints".desc)
  }

  def topPlayerAveragePoints(players: DataFrame): DataFrame = {
    import players.sparkSession.implicits._
    rankingPlayers(players) groupBy $"region" agg avg("leaguePoints") withColumnRenamed("avg(leaguePoints)", "Average Points")
  }

  def topNPlayer(players: DataFrame, n: Int): DataFrame = {
    import players.sparkSession.implicits._
    players select($"*", rank over Window.partitionBy($"region").orderBy($"leaguePoints".desc) alias "top") filter ($"rank" <= n) orderBy($"region".asc, $"leaguePoints".desc)
  }

  def topNPlayerAveragePoints(players: DataFrame, n: Int): DataFrame = {
    import players.sparkSession.implicits._
    topNPlayer(players, n) groupBy $"region" agg avg("leaguePoints") withColumnRenamed("avg(leaguePoints)", "Average Points")
  }

  def totalMatchesPerPlayer(players: DataFrame): DataFrame = {
    import players.sparkSession.implicits._
    players orderBy($"region".asc, $"leaguePoints".desc) withColumn("totalMatches", $"wins" + $"losses")
  }

  def averageMatches(players: DataFrame): DataFrame = {
    import players.sparkSession.implicits._
    totalMatchesPerPlayer(players) groupBy $"region" agg avg($"totalMatches")
  }

  def winRatePerPlayer(players: DataFrame): DataFrame = {
    import players.sparkSession.implicits._
    players orderBy($"region".asc, $"leaguePoints".desc) withColumn("winRate", ($"wins" / ($"losses" + $"wins")) * 100)
  }

  def averageWinRatePerRegion(players: DataFrame): DataFrame = {
    import players.sparkSession.implicits._
    winRatePerPlayer(players) groupBy $"region" agg avg($"winRate") withColumnRenamed("avg(winRate)", "Average Win Rate")
  }

  // Matches Queries
  def rankedClassicGames(matches: DataFrame): DataFrame = {
    import matches.sparkSession.implicits._
    matches filter $"gameMode" === "CLASSIC" && $"gameType" === "MATCHED_GAME"
  }

  def averageMatchTimePerRegion(matches: DataFrame): DataFrame = {
    import matches.sparkSession.implicits._
    matches groupBy $"region" agg avg($"gameDuration") withColumn("avg(gameDuration)", $"avg(gameDuration)" / 60) withColumnRenamed("avg(gameDuration)", "Average Duration (min)")
  }

  def visionScorePerPlayer(matches: DataFrame): DataFrame = {
    import matches.sparkSession.implicits._
    matches orderBy $"region" withColumn("visionScore", $"participants.stats.visionScore")
  }

  def averageVisionScore(matches: DataFrame): DataFrame = {
    import matches.sparkSession.implicits._
    val getAverageVision = udf((totalVision: Seq[Long]) => totalVision.sum[Long] / totalVision.size)
    visionScorePerPlayer(matches) withColumn("Average Vision Score", getAverageVision($"visionScore"))
  }

  def allStats(matches: DataFrame): DataFrame = {
    import matches.sparkSession.implicits._
    matches.orderBy($"region".desc).withColumn("identities", explode($"participantIdentities")).alias("ident")
      .join(matches.withColumn("newParticipants", explode($"participants")).as("st"), $"identities.participantId" === $"st.newParticipants.participantId"
        && $"ident.gameId" === $"st.gameId")
      .select($"ident.region", $"ident.gameId", $"identities.player" as "player", $"newParticipants.stats" as "stats", $"identities.participantId")

  }

  def playerStats(players: DataFrame, stats: DataFrame): DataFrame = {
    import players.sparkSession.implicits._

    rankingPlayers(players).orderBy($"region".desc).as("pl").join(stats, $"pl.summonerName" === $"player.summonerName" && $"pl.region" === $"ident.region")
      .select($"pl.region", $"gameId", $"summonerName", $"wins", $"losses", $"stats", $"top")
  }

  //TOOD: KDA de los players
  def firstBloodParticipant(playerStats: DataFrame): DataFrame = {
    import playerStats.sparkSession.implicits._
    (playerStats groupBy($"region", $"summonerName", $"top") agg count($"stats.firstBloodAssist" === true || $"stats.firstBloodKill" === true)
      withColumnRenamed("count(((stats.firstBloodAssist = true) OR (stats.firstBloodKill = true)))", "First blood participant")
      orderBy($"region".asc, $"First blood participant".desc))
  }
}
