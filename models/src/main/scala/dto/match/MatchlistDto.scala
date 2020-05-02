package dto.`match`

case class MatchlistDto(matches: List[MatchReferenceDto], totalGames: Int,
                        startIndex: Int, endIndex: Int)
