package dto.`match`


//Esencialmente nos interesa el gameId para despues acceder a los detalles de la partida
case class MatchReferenceDto(lane: String, gameId: Long, champion: Int, platformId: String, season: Int,
                             queue: Int, role: String, timestamp: Long)