package dto.`match`

import dto.team.TeamStatsDto


case class MatchDto(seasonId: Int, queueId: Int, gameId: Long, participantIdentities: List[ParticipantIdentityDto],
                    gameVersion: String, platformId: String, gameMode: String, mapId: Int, gameType: String,
                    teams: List[TeamStatsDto], participants: List[ParticipantDto], gameDuration: Long, gameCreation: Long)
