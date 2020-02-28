package dto.player

case class SummonerDTO(profileIconId: Option[Int], name: Option[String], puuid: Option[String],
                       summonerLevel: Option[Long], revisionDate: Option[Long], id: Option[String],
                       accountId: Option[String])
