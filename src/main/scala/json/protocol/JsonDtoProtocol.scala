package json.protocol

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import dto.{LeagueItemDTO, LeagueListDTO, MiniSeriesDTO, SummonerDTO}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonDtoProtocol extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val summonerProtocol: RootJsonFormat[SummonerDTO] = jsonFormat7(SummonerDTO)
  implicit val miniSeriesDTO: RootJsonFormat[MiniSeriesDTO] = jsonFormat4(MiniSeriesDTO)
  implicit val leagueItemDTO: RootJsonFormat[LeagueItemDTO] = jsonFormat10(LeagueItemDTO)
  implicit val leagueListDTO: RootJsonFormat[LeagueListDTO] = jsonFormat5(LeagueListDTO)
}
