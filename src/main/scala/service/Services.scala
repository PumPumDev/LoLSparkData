package service

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, headers}
import akka.http.scaladsl.unmarshalling.Unmarshal
import dto.player.{LeagueListDTO, SummonerDTO}
import usefull.FilesManagement._
import usefull.LoadObject._
import usefull.Regions
import usefull.Regions.Region
import usefull.Uris._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

//TODO Bring the API calls here
object Services {

  def getChallengerPlayers: Map[Region, LeagueListDTO] =
    challengerPlayersFile match {
      case value if value.exists() =>
        loadJsonData[Map[Region, LeagueListDTO]](value) //Importante pasarle el tipo para que sepa cómo serializar
      case value =>
        saveDataAsJson[Map[Region, LeagueListDTO]](Regions.values.map(reg => {
          (reg, Await.result(Unmarshal[HttpResponse](Await.result(Http().singleRequest(HttpRequest(uri = uriProtocol + reg + riotChallengerUri)
            .withHeaders(headers.RawHeader(riotToken._1, riotToken._2))): Future[HttpResponse], Duration.Inf): HttpResponse)
            .to[LeagueListDTO], Duration.Inf): LeagueListDTO) //Da un warning por la fecha, al parecer nos devuelven la fecha con un format distinto
        }).toMap[Region, LeagueListDTO])(value)
    }

  def getChallengerSummoners(players: Map[Region, LeagueListDTO]): Map[Region, List[SummonerDTO]] =
    challengerSummonersFile match {
      case value if value.exists() =>
        loadJsonData[Map[Region, List[SummonerDTO]]](value)
      case value =>
        saveDataAsJson[Map[Region, List[SummonerDTO]]](players.map(mapEntry =>
          (mapEntry._1, mapEntry._2.entries.map(item => {
            Thread.sleep(2000)
            Await.result(
              Unmarshal[HttpResponse](
                Await.result(
                  Http().singleRequest(
                    HttpRequest(uri = uriProtocol + mapEntry._1 + riotSummonerUri + item.summonerId)
                      .withHeaders(headers.RawHeader(riotToken._1, riotToken._2))), Duration.Inf))
                .to[SummonerDTO], Duration.Inf)
          }): List[SummonerDTO])))(value)
    }
}
