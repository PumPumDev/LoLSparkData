package json.protocol

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import dto.`match`.{MatchDto, MatchReferenceDto, MatchlistDto}
import dto.player.{LeagueItemDTO, LeagueListDTO, MiniSeriesDTO, SummonerDTO}
import spray.json.{DefaultJsonProtocol, NullOptions, RootJsonFormat}

trait JsonCustomProtocol extends SprayJsonSupport with DefaultJsonProtocol with NullOptions {
  implicit val summonerDto: RootJsonFormat[SummonerDTO] = jsonFormat7(SummonerDTO)
  implicit val miniSeriesDTO: RootJsonFormat[MiniSeriesDTO] = jsonFormat4(MiniSeriesDTO)
  implicit val leagueItemDTO: RootJsonFormat[LeagueItemDTO] = jsonFormat10(LeagueItemDTO)
  implicit val leagueListDTO: RootJsonFormat[LeagueListDTO] = jsonFormat5(LeagueListDTO)
  implicit val matchReferenceDto: RootJsonFormat[MatchReferenceDto] = jsonFormat8(MatchReferenceDto)
  implicit val matchlistDto: RootJsonFormat[MatchlistDto] = jsonFormat4(MatchlistDto)
  implicit val matchDto: RootJsonFormat[MatchDto] = jsonFormat10(MatchDto)

}
