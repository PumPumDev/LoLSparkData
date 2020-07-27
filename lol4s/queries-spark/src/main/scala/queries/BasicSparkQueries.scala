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

  def totalMatchesPerPlayer(players: DataFrame): DataFrame = {
    import players.sparkSession.implicits._
    players withColumn("totalMatches", $"wins" + $"losses")  orderBy($"region".asc, $"totalMatches".desc)
  }

  def averageMatches(players: DataFrame): DataFrame = {
    import players.sparkSession.implicits._
    totalMatchesPerPlayer(players) groupBy $"region" agg avg($"totalMatches") withColumnRenamed ("avg(totalMatches)", "average_matches")
  }

  def winRatePerPlayer(players: DataFrame): DataFrame = {
    import players.sparkSession.implicits._
    players withColumn("winRate", ($"wins" / ($"losses" + $"wins")) * 100) orderBy($"region".asc, $"winRate".desc)
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


  def firstBloodParticipant(playerStats: DataFrame): DataFrame = {
    import playerStats.sparkSession.implicits._
    (playerStats groupBy($"region", $"summonerName", $"top") agg count($"stats.firstBloodAssist" === true || $"stats.firstBloodKill" === true)
      withColumnRenamed("count(((stats.firstBloodAssist = true) OR (stats.firstBloodKill = true)))", "First blood participant")
      orderBy($"region".asc, $"First blood participant".desc))
  }

  def kdaPlayers(playerStats: DataFrame) : DataFrame = {
    import playerStats.sparkSession.implicits._
    val kda = udf((kills: Int, deaths: Int, assists: Int) => if (deaths!=0) (kills+assists)/deaths else kills+assists)
    (playerStats groupBy($"region", $"summonerName", $"top") agg avg(kda($"stats.kills",$"stats.deaths",$"stats.assists"))
      withColumnRenamed ("avg(UDF(stats.kills, stats.deaths, stats.assists))","KDA")
      orderBy($"region".asc))
  }

  def averageGoldEarned(playerStats: DataFrame): DataFrame = {
    import playerStats.sparkSession.implicits._
    (playerStats groupBy($"region", $"summonerName", $"top") agg avg($"stats.goldEarned")
      withColumnRenamed("avg(stats.goldEarned)", "goldEarned")
      orderBy($"region"))
  }

  def averageDmgDeal(playerStats: DataFrame): DataFrame = {
    import playerStats.sparkSession.implicits._
    (playerStats groupBy($"region", $"summonerName", $"top") agg avg($"stats.totalDamageDealtToChampions")
      withColumnRenamed("avg(stats.totalDamageDealtToChampions)", "damageDeal")
      orderBy($"region"))
  }

  def averageMinionsKilled(playerStats: DataFrame): DataFrame = {
    import playerStats.sparkSession.implicits._
    (playerStats groupBy($"region", $"summonerName", $"top") agg avg($"stats.totalMinionsKilled")
      withColumnRenamed("avg(stats.totalMinionsKilled)", "minionsKilled")
      orderBy ($"region"))
  }

  def averageTotalHeal(playerStats: DataFrame): DataFrame = {
    import playerStats.sparkSession.implicits._
    (playerStats groupBy($"region", $"summonerName", $"top") agg avg($"stats.totalHeal")
      withColumnRenamed("avg(stats.totalHeal)", "totalHeal")
      orderBy ($"region"))
  }

  def averageVisionScore(playerStats: DataFrame): DataFrame = {
    import playerStats.sparkSession.implicits._
    (playerStats groupBy($"region", $"summonerName", $"top") agg avg($"stats.visionScore")
      withColumnRenamed("avg(stats.visionScore)", "visionScore")
      orderBy ($"region"))
  }

  def averageNeutralMinionsKilled(playerStats: DataFrame): DataFrame = {
    import playerStats.sparkSession.implicits._
    (playerStats groupBy($"region", $"summonerName", $"top") agg avg($"stats.neutralMinionsKilled")
      withColumnRenamed("avg(stats.neutralMinionsKilled)", "neutralMinionsKilled")
      orderBy ($"region"))
  }

  def averageCCtime(playerStats: DataFrame): DataFrame = {
    import playerStats.sparkSession.implicits._
    (playerStats groupBy($"region", $"summonerName", $"top") agg avg($"stats.timeCCingOthers")
      withColumnRenamed("avg(stats.timeCCingOthers)", "timeCCing")
      orderBy ($"region"))
  }
}
