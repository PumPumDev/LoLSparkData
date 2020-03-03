package usefull

import usefull.LoadObject._

object Uris {
  val uriProtocol: String = "https://"
  val riotChallengerUri: String = ".api.riotgames.com/lol/league/v4/challengerleagues/by-queue/RANKED_SOLO_5x5"
  val riotSummonerUri: String = ".api.riotgames.com/lol/summoner/v4/summoners/"
  val riotMatchlistUri: String = ".api.riotgames.com/lol/match/v4/matchlists/by-account/"
  val riotMatchUri: String = ".api.riotgames.com/lol/match/v4/matches/"
  val riotToken: (String, String) = (config.getString("riotToken"), config.getString("apiKey"))
}
