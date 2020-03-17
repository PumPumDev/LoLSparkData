package queries

import dto.`match`.MatchDto
import dto.player.{LeagueItemDTO, LeagueListDTO}
import usefull.Region

object BasicScalaQueries {
  //About Players
  def topNPlayer(players: Map[Region, LeagueListDTO], numPlayers: Int): Map[Region, List[LeagueItemDTO]] =
    players.map {
      case (region, o) => (region, o.entries.sortWith(_.leaguePoints > _.leaguePoints).take(numPlayers))
    }

  def topNPlayerAveragePoints(players: Map[Region, LeagueListDTO], numPlayers: Int): Map[Region, Int] =
    players.map {
      case (region, o) => (region, o.entries.sortWith(_.leaguePoints > _.leaguePoints).take(numPlayers)
        .foldLeft(0)(_ + _.leaguePoints) / o.entries.length)
    }

  def totalMatchesPerPlayer(players: Map[Region, LeagueListDTO]): Map[Region, List[(LeagueItemDTO, Int)]] =
    players.map {
      case (region, o) => (region, o.entries.map(item => (item, item.wins + item.losses)))
    }

  def averageMatchesPerRegion(players: Map[Region, LeagueListDTO]): Map[Region, Int] =
    players.map {
      case (region, o) => (region, o.entries.foldLeft(0)((int, item) => int + item.wins + item.losses) / o.entries.length)
    }

  def winRateTopNPlayers(players: Map[Region, LeagueListDTO], numPlayers: Int): Map[Region, List[(LeagueItemDTO, Double)]] =
    players.map {
      case (region, o) => (region, o.entries.sortWith(_.leaguePoints > _.leaguePoints).take(numPlayers)
        .map(item => (item, setScale(winRate(item), 2))))
    }

  def averageWinRatePerRegion(players: Map[Region, LeagueListDTO]): Map[Region, Double] =
    players.map {
      case (region, o) => (region, setScale(o.entries.foldLeft(0d)((op, item) => op + winRate(item)) / o.entries.length, 2))
    }

  private def winRate(leagueItemDto: LeagueItemDTO): Double =
    (leagueItemDto.wins.toDouble / (leagueItemDto.wins + leagueItemDto.losses)) * 100

  //About matches
  def averageMatchesTimePerRegion(matches: Map[Region, List[MatchDto]]): Map[Region, Double] =
    matches.map {
      case (region, dtoes) => (region, setScale((dtoes.foldLeft(0d)(_ + _.gameDuration) / dtoes.length) / 60, 2))
    }

  def averageVisionScorePerRegion(matches: Map[Region, List[MatchDto]]): Map[Region, Double] =
    matches.map {
      case (region, dtoes) => (region, setScale(dtoes.flatMap(_.participants.map(_.stats.get.visionScore.get)).foldLeft(0d)(_ + _) / dtoes.length, 2))
    }

  private def setScale(double: Double, scale: Int): Double =
    BigDecimal(double).setScale(scale, BigDecimal.RoundingMode.HALF_UP).toDouble

  def kdaTopNPlayers(players: Map[Region, LeagueListDTO], matches: Map[Region, List[MatchDto]], numPlayers: Int): Map[Region, List[(String, Option[Double])]] =
    players.map {
      case (region, o) => (region, o.entries.sortWith(_.leaguePoints > _.leaguePoints).take(numPlayers)
        .map(pl => (pl.summonerName, kdaCalculate(matches(region)
          .filter(_.participantIdentities.map(_.player.get.summonerName.get).contains(pl.summonerName)), pl.summonerName))))
    }


  private def kdaCalculate(matches: List[MatchDto], sumName: String): Option[Double] =
    matches.flatMap(mat => mat.participants.filter(_.participantId.get == mat.participantIdentities.filter(_.player.get.summonerName.get == sumName).head.participantId.get)
      .map(part => (part.stats.get.kills.get, part.stats.get.deaths.get, part.stats.get.assists.get)))
      .reduceOption((a1, a2) => (a1._1 + a2._1, a1._2 + a2._2, a1._3 + a2._3)) match {
      case Some(value) => Some(setScale((value._1 + value._3).toDouble / value._2, 2))
      case None => None
    }
}
