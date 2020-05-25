package dto.`match`

case class MatchlistDTO(matches: List[MatchReferenceDTO], totalGames: Int,
                        startIndex: Int, endIndex: Int)
