package dto.`match`

case class MatchlistDto(matches: Option[List[MatchReferenceDto]], totalGames: Option[Int], startIndex: Option[Int], endIndex: Option[Int])
