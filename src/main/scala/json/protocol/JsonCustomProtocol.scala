package json.protocol

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import dto.`match`._
import dto.game.properties.{MasteryDto, ParticipantTimelineDto, RuneDto}
import dto.player.{LeagueItemDTO, LeagueListDTO, MiniSeriesDTO, SummonerDTO}
import dto.team.{TeamBansDto, TeamStatsDto}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonCustomProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  /**
   * It is important to define the formats properly with the classes dependencies or we will get a NullPointer when we try to Unmarshall
   */

  implicit val summonerDto: RootJsonFormat[SummonerDTO] = jsonFormat7(SummonerDTO)
  implicit val miniSeriesDTO: RootJsonFormat[MiniSeriesDTO] = jsonFormat4(MiniSeriesDTO)
  implicit val leagueItemDTO: RootJsonFormat[LeagueItemDTO] = jsonFormat10(LeagueItemDTO)
  implicit val leagueListDTO: RootJsonFormat[LeagueListDTO] = jsonFormat5(LeagueListDTO)
  implicit val matchReferenceDto: RootJsonFormat[MatchReferenceDto] = jsonFormat8(MatchReferenceDto)
  implicit val matchlistDto: RootJsonFormat[MatchlistDto] = jsonFormat4(MatchlistDto)
  implicit val runeDto: RootJsonFormat[RuneDto] = jsonFormat2(RuneDto)
  implicit val masteryDto: RootJsonFormat[MasteryDto] = jsonFormat2(MasteryDto)
  implicit val teamBansDto: RootJsonFormat[TeamBansDto] = jsonFormat2(TeamBansDto)
  implicit val teamStatsDto: RootJsonFormat[TeamStatsDto] = jsonFormat16(TeamStatsDto)
  implicit val participantTimelineDto: RootJsonFormat[ParticipantTimelineDto] = jsonFormat10(ParticipantTimelineDto)
  implicit val participantStatsDto: RootJsonFormat[ParticipantStatsDto] = jsonFormat22(ParticipantStatsDto)
  implicit val participantDto: RootJsonFormat[ParticipantDto] = jsonFormat10(ParticipantDto)
  implicit val playerDto: RootJsonFormat[PlayerDto] = jsonFormat8(PlayerDto)
  implicit val participantIdentityDto: RootJsonFormat[ParticipantIdentityDto] = jsonFormat2(ParticipantIdentityDto)
  implicit val matchDto: RootJsonFormat[MatchDto] = jsonFormat13(MatchDto)

}
