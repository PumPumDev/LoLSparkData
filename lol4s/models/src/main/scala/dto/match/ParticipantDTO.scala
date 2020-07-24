package dto.`match`

import dto.game.properties.{MasteryDTO, ParticipantTimelineDTO, RuneDTO}

case class ParticipantDTO(stats: Option[ParticipantStatsDTO], participantId: Option[Int], runes: Option[List[RuneDTO]], timeline: Option[ParticipantTimelineDTO],
                          teamId: Option[Int], spell2Id: Option[Int], masteries: Option[List[MasteryDTO]], highestAchievedSeasonTier: Option[String], spell1Id: Option[Int],
                          championId: Option[Int])

/*
teamId: Possible values 100 for blue side and 200 for red side
 */
