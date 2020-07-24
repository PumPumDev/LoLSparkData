package dto.`match`

import dto.team.TeamStatsDTO


case class MatchDTO(seasonId: Int, queueId: Int, gameId: Long, participantIdentities: List[ParticipantIdentityDTO],
                    gameVersion: String, platformId: String, gameMode: String, mapId: Int, gameType: String,
                    teams: List[TeamStatsDTO], participants: List[ParticipantDTO], gameDuration: Long, gameCreation: Long)
