package queries

import dto.RegionDTO
import dto.`match`.MatchDto
import dto.player.{LeagueItemDTO, LeagueListDTO}

object BasicScalaQueries {
  //About Players
  def topNPlayer(players: Map[RegionDTO, LeagueListDTO], numPlayers: Int): Map[RegionDTO, List[LeagueItemDTO]] =
    players.map {
      case (region, o) => (region, o.entries.sortWith(_.leaguePoints > _.leaguePoints).take(numPlayers))
    }

  def topNPlayerAveragePoints(players: Map[RegionDTO, LeagueListDTO], numPlayers: Int): Map[RegionDTO, Int] =
    players.map {
      case (region, o) => (region, o.entries.sortWith(_.leaguePoints > _.leaguePoints).take(numPlayers)
        .foldLeft(0)(_ + _.leaguePoints) / o.entries.length)
    }

  def totalMatchesPerPlayer(players: Map[RegionDTO, LeagueListDTO]): Map[RegionDTO, List[(LeagueItemDTO, Int)]] =
    players.map {
      case (region, o) => (region, o.entries.map(item => (item, item.wins + item.losses)))
    }

  def averageMatchesPerRegion(players: Map[RegionDTO, LeagueListDTO]): Map[RegionDTO, Int] =
    players.map {
      case (region, o) => (region, o.entries.foldLeft(0)((int, item) => int + item.wins + item.losses) / o.entries.length)
    }

  def winRateTopNPlayers(players: Map[RegionDTO, LeagueListDTO], numPlayers: Int): Map[RegionDTO, List[(LeagueItemDTO, Double)]] =
    players.map {
      case (region, o) => (region, o.entries.sortWith(_.leaguePoints > _.leaguePoints).take(numPlayers)
        .map(item => (item, setScale(winRate(item), 2))))
    }

  private def winRate(leagueItemDto: LeagueItemDTO): Double =
    (leagueItemDto.wins.toDouble / (leagueItemDto.wins + leagueItemDto.losses)) * 100

  private def setScale(double: Double, scale: Int): Double =
    BigDecimal(double).setScale(scale, BigDecimal.RoundingMode.HALF_UP).toDouble

  def averageWinRatePerRegion(players: Map[RegionDTO, LeagueListDTO]): Map[RegionDTO, Double] =
    players.map {
      case (region, o) => (region, setScale(o.entries.foldLeft(0d)((op, item) => op + winRate(item)) / o.entries.length, 2))
    }

  //About matches
  def averageMatchesTimePerRegion(matches: Map[RegionDTO, List[MatchDto]]): Map[RegionDTO, Double] =
    matches.map {
      case (region, dtoes) => (region, setScale((dtoes.foldLeft(0d)(_ + _.gameDuration) / dtoes.length) / 60, 2))
    }

  def averageVisionScorePerRegion(matches: Map[RegionDTO, List[MatchDto]]): Map[RegionDTO, Double] =
    matches.map {
      case (region, dtoes) => (region, setScale(dtoes.flatMap(_.participants.map(_.stats.get.visionScore.get)).foldLeft(0d)(_ + _) / dtoes.length, 2))
    }

  def kdaTopNPlayers(players: Map[RegionDTO, LeagueListDTO], matches: Map[RegionDTO, List[MatchDto]], numPlayers: Int): Map[RegionDTO, List[(String, Option[Double])]] =
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
