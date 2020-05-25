package dto.game.properties

case class ParticipantTimelineDTO(lane: Option[String], participantId: Option[Int], csDiffPerMinDeltas: Option[Map[String, Double]], goldPerMinDeltas: Option[Map[String, Double]],
                                  xpDiffPerMinDeltas: Option[Map[String, Double]], creepsPerMinDeltas: Option[Map[String, Double]],
                                  xpPerMinDeltas: Option[Map[String, Double]], role: Option[String], damageTakenDiffPerMinDeltas: Option[Map[String, Double]],
                                  damageTakenPerMinDeltas: Option[Map[String, Double]])

/*
lane: Participant's calculated lane. MID and BOT are legacy values. (Legal values: MID, MIDDLE, TOP, JUNGLE, BOT, BOTTOM)
role: Participant's calculated role. (Legal values: DUO, NONE, SOLO, DUO_CARRY, DUO_SUPPORT)
 */