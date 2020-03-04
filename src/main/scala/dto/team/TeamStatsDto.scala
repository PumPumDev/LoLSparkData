package dto.team

case class TeamStatsDto(firstDragon: Boolean, firstInhibitor: Boolean, bans: List[TeamBansDto], baronKills: Int,
                        firstRiftHerald: Boolean, firstBaron: Boolean, riftHeraldKills: Int, firstBlood: Boolean,
                        teamId: Int, firstTower: Boolean, vilemawKills: Int, inhibitorKills: Int, towerKills: Int,
                        dominionVictoryScore: Int, win: String, dragonKills: Int)

//Win values: Fail/Win
