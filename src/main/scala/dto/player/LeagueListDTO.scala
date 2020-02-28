package dto.player

case class LeagueListDTO(leagueId: String, tier: String, entries: List[LeagueItemDTO],
                         queue: String, name: String)
