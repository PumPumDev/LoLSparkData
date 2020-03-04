package service

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, headers}
import akka.http.scaladsl.unmarshalling.Unmarshal
import dto.`match`.{MatchDto, MatchReferenceDto, MatchlistDto}
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
  private val maxRequestRateAchieve = 429

  def getChallengerPlayers: Map[Region, LeagueListDTO] =
    challengerPlayersFile match {
      case value if value.exists() =>
        println("Loading data from local JSON file")

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
        println("Loading data from local JSON file")
        loadJsonData[Map[Region, List[SummonerDTO]]](value)
      case value =>
        println("Estimated time to get the data from the API (with a Personal API key): 20 min")

        saveDataAsJson[Map[Region, List[SummonerDTO]]](players.map(mapEntry =>
          (mapEntry._1, mapEntry._2.entries.map(item => {

            val request: HttpRequest =
              HttpRequest(uri = uriProtocol + mapEntry._1 + riotSummonerUri + item.summonerId)
                .withHeaders(headers.RawHeader(riotToken._1, riotToken._2))

            Await.result(
              Http().singleRequest(request), Duration.Inf) match {
              case value if value.status.intValue() == maxRequestRateAchieve =>
                Await.result(Unmarshal[HttpResponse](retryRequest(value)(request))
                  .to[SummonerDTO], Duration.Inf)
              case value if value.status.isSuccess() => Await.result(
                Unmarshal[HttpResponse](value)
                  .to[SummonerDTO], Duration.Inf)
              case _ => throw new RuntimeException("Server Error")
            }
          }): List[SummonerDTO])))(value)
    }

  def getChallengerMatchlist(summoners: Map[Region, List[SummonerDTO]]): Map[Region, List[(SummonerDTO, MatchlistDto)]] =
    challengerMatchlistFile match {
      case value if value.exists() =>
        println("Loading data from local JSON file")

        loadJsonData[Map[Region, List[(SummonerDTO, MatchlistDto)]]](value)

      case file =>
        println("Estimated time to get the data from the API (with a Personal API key): 30 min")
        saveDataAsJson[Map[Region, List[(SummonerDTO, MatchlistDto)]]](summoners.map {
          case (region, os) =>
            (region, os.map(value => (value, {
              val httpRequest: HttpRequest = HttpRequest(uri = uriProtocol + region + riotMatchlistUri + value.accountId)
                .withHeaders(headers.RawHeader(riotToken._1, riotToken._2))

              Await.result(Http().singleRequest(httpRequest), Duration.Inf) match {
                case httpResponse: HttpResponse if httpResponse.status.intValue() == maxRequestRateAchieve =>
                  println(httpResponse)
                  retryRequest(httpResponse)(httpRequest) match {
                    case retryResponse: HttpResponse if retryResponse.status.isSuccess() =>
                      Await.result(Unmarshal[HttpResponse](retryResponse)
                        .to[MatchlistDto], Duration.Inf)
                    case retryResponse =>
                      retryResponse.discardEntityBytes()
                      System.err.println("Server retry was failed")
                      println(retryResponse)
                      MatchlistDto(List(), 0, 0, 0) //Default instance if it fails
                  }

                case httpResponse: HttpResponse if httpResponse.status.isSuccess() =>

                  Await.result(Unmarshal[HttpResponse](httpResponse).to[MatchlistDto], Duration.Inf)

                case e =>
                  e.discardEntityBytes()
                  println(e)
                  System.err.println("Server responses was failed")
                  MatchlistDto(List(), 0, 0, 0) //Default instance if it fails
              }
            })))
        })(file)
    }

  def getChallengerMatches(matchesLists: Map[Region, List[(SummonerDTO, MatchlistDto)]]): Map[Region, List[MatchDto]] =
    challengerMatchesFile match {
      case file if file.exists() =>
        println("Loading data from local JSON file")

        loadJsonData[Map[Region, List[MatchDto]]](file)

      case file =>
        println("Estimated time to get the data from the API (with a Personal API key): +40 min")

        saveDataAsJson[Map[Region, List[MatchDto]]](
          matchesLists.map {
            case (region, list) => (region, list.flatMap {
              case (_, dto) => dto.matches
            }.distinct.take(400).map((reference: MatchReferenceDto) => {
              val httpRequest: HttpRequest = HttpRequest(uri = uriProtocol + region + riotMatchUri + reference.gameId)
                .withHeaders(headers.RawHeader(riotToken._1, riotToken._2))

              Await.result(Http().singleRequest(httpRequest), Duration.Inf) match {
                case httpResponse: HttpResponse if httpResponse.status.intValue() == maxRequestRateAchieve =>
                  println(httpResponse)
                  retryRequest(httpResponse)(httpRequest) match {
                    case retryResponse: HttpResponse if retryResponse.status.isSuccess() =>
                      Await.result(Unmarshal[HttpResponse](retryResponse)
                        .to[MatchDto], Duration.Inf)

                    case retryResponse =>
                      retryResponse.discardEntityBytes()
                      System.err.println("Server retry was failed")
                      println(retryResponse)
                      MatchDto(0, 0, 0, List(), "", "", "", 0, "", List(), List(), 0, 0) //Default instance if it fails
                  }


                case httpResponse: HttpResponse if httpResponse.status.isSuccess() =>
                  Await.result(Unmarshal[HttpResponse](httpResponse).to[MatchDto], Duration.Inf)


                case e =>
                  e.discardEntityBytes()
                  println(e)
                  System.err.println("Server responses was failed")
                  MatchDto(0, 0, 0, List(), "", "", "", 0, "", List(), List(), 0, 0) //Default instance if it fails
              }
            }))
          }
        )(file)
    }

  private def retryRequest(httpResponse: HttpResponse)(httpRequest: HttpRequest): HttpResponse = {
    val timeToWait: Int = httpResponse.headers.filter(header => header.is("retry-after"))
      .map(header => header.value().toInt) match {
      case list if list.nonEmpty => list.head
      case _ => 105
    }
    httpResponse.discardEntityBytes() //We have to process response even when it has no data
    println("Max Rate Achieve. We are waiting " + timeToWait + " seconds to continue")
    Thread.sleep(timeToWait * 1000 + 50) //We convert it into milliseconds
    Await.result(Http().singleRequest(httpRequest), Duration.Inf)
  }
}
