package dto.`match`

//Esencialmente nos interesa el gameId para despues acceder a los detalles de la partida
case class MatchReferenceDTO(lane: String, gameId: Long, champion: Int, platformId: String, season: Int,
                             queue: Int, role: String, timestamp: Long) {
  /*
  We override equals and hashCode to make two games equals if they have the same gameId
   */
  override def equals(obj: Any): Boolean = obj match {
    case myClass: MatchReferenceDTO => myClass.gameId.equals(gameId)
  }

  override def hashCode(): Int = gameId.hashCode()
}