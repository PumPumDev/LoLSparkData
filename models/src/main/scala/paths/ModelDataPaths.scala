package paths

import dto.RegionDTO

object ModelDataPaths {
  def getPlayerPath(outputPath: String, region: RegionDTO): String =
    s"$outputPath/players/$region.data"

  def getSummonerPath(outputPath: String, region: RegionDTO): String =
    s"$outputPath/summoners/$region.data"

  def getMatchesReferencesPath(outputPath: String, region: RegionDTO) =
    s"$outputPath/match_references/$region.data"

  def getMatchesPath(outputPath: String, region: RegionDTO): String =
    s"$outputPath/matches/$region.data"
}
