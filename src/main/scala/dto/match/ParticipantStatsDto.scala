package dto.`match`


case class ParticipantStatsDto(firstBloodAssist: Option[Boolean], visionScore: Option[Long], kills: Option[Int],
                               neutralMinionsKilled: Option[Int], damageDealtToTurrets: Option[Int],
                               largestKillingSpree: Option[Int],
                               assists: Option[Int], combatPlayerScore: Option[Int],
                               totalDamageDealtToChampions: Option[Long],
                               win: Option[Boolean],
                               objectivePlayerScore: Option[Int],
                               deaths: Option[Int],
                               firstBloodKill: Option[Boolean],
                               goldEarned: Option[Long], killingSprees: Option[Int],
                               firstTowerAssist: Option[Boolean],
                               firstTowerKill: Option[Boolean],
                               inhibitorKills: Option[Int], firstInhibitorAssist: Option[Boolean],
                               totalHeal: Option[Long], totalMinionsKilled: Option[Int],
                               timeCCingOthers: Option[Long]) {
  /*
  We must put all fields with Option[T] because there are different types of games with different stats

  We choose the 22 most important variables in game
   */

}
