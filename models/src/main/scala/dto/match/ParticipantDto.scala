package dto.`match`

import dto.game.properties.{MasteryDto, ParticipantTimelineDto, RuneDto}

case class ParticipantDto(stats: Option[ParticipantStatsDto], participantId: Option[Int], runes: Option[List[RuneDto]], timeline: Option[ParticipantTimelineDto],
                          teamId: Option[Int], spell2Id: Option[Int], masteries: Option[List[MasteryDto]], highestAchievedSeasonTier: Option[String], spell1Id: Option[Int],
                          championId: Option[Int])

/*
teamId: Possible values 100 for blue side and 200 for red side
 */
