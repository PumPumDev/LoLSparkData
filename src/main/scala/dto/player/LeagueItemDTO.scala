package dto.player

case class LeagueItemDTO(summonerName: String, hotStreak: Boolean, wins: Int,
                         veteran: Boolean, losses: Int, freshBlood: Boolean, inactive: Boolean,
                         rank: String, summonerId: String, leaguePoints: Int)
