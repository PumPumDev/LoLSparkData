package dto.`match`

//TODO: Add the rest of the match attributes
case class MatchDto(seasonId: Int, queueId: Int, gameId: Long, gameVersion: String, platformId: String,
                    gameMode: String, mapId: Int, gameType: String, gameDuration: Long, gameCreation: Long)
