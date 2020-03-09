package service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, headers}
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.scalalogging.Logger
import dto.`match`.{MatchDto, MatchReferenceDto, MatchlistDto}
import dto.player.{LeagueListDTO, SummonerDTO}
import usefull.FilesManagement._
//import usefull.LoadObject._
import usefull.Regions
import usefull.Regions.Region
import usefull.Uris._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

//TODO Bring the API calls here
object Services {
  private val maxRequestRateAchieve = 429

  private val logger = Logger("Service")

  //TODO: Cambiar a Option[T] en lugar de crear instancias por defecto

  def getChallengerPlayers(pathFile: String)
                          (implicit actorSystem: ActorSystem): Map[Region, LeagueListDTO] =
    getFile(pathFile) match {
      case value if value.exists() =>
        logger.info("Loading data from local JSON file")

        loadJsonData[Map[Region, LeagueListDTO]](value) //Importante pasarle el tipo para que sepa cómo serializar
      case value =>
        saveDataAsJson[Map[Region, LeagueListDTO]](Regions.values.map(reg => {
          (reg, Await.result(Unmarshal[HttpResponse](Await.result(Http().singleRequest(HttpRequest(uri = uriProtocol + reg + riotChallengerUri)
            .withHeaders(headers.RawHeader(riotToken._1, riotToken._2))): Future[HttpResponse], Duration.Inf): HttpResponse)
            .to[LeagueListDTO], Duration.Inf): LeagueListDTO) //Da un warning por la fecha, al parecer nos devuelven la fecha con un format distinto
        }).toMap[Region, LeagueListDTO])(value)
    }

  def getChallengerSummoners(pathFile: String, players: Map[Region, LeagueListDTO])
                            (implicit actorSystem: ActorSystem): Map[Region, List[SummonerDTO]] =
    getFile(pathFile) match {
      case value if value.exists() =>
        logger.info("Loading data from local JSON file")
        loadJsonData[Map[Region, List[SummonerDTO]]](value)
      case value =>
        logger.info("Estimated time to get the data from the API (with a Personal API key): 20 min")

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
              case e =>
                e.discardEntityBytes()
                logger.error(e.toString())
                logger.error("Server responses was failed")
                SummonerDTO(0, "", "", 0, 0, "", "") //Default instance if it fails
            }
          }): List[SummonerDTO])))(value)
    }

  private def retryRequest(httpResponse: HttpResponse)(httpRequest: HttpRequest)
                          (implicit actorSystem: ActorSystem): HttpResponse = {
    val timeToWait: Int = httpResponse.headers.filter(header => header.is("retry-after"))
      .map(header => header.value().toInt) match {
      case list if list.nonEmpty => list.head
      case _ => 105
    }
    httpResponse.discardEntityBytes() //We have to process response even when it has no data
    logger.info("Max Rate Achieve. We are waiting " + timeToWait + " seconds to continue")
    Thread.sleep(timeToWait * 1000 + 50) //We convert it into milliseconds
    Await.result(Http().singleRequest(httpRequest), Duration.Inf)
  }

  def getChallengerMatchlist(pathFile: String, summoners: Map[Region, List[SummonerDTO]])
                            (implicit actorSystem: ActorSystem): Map[Region, List[(SummonerDTO, MatchlistDto)]] =
    getFile(pathFile) match {
      case value if value.exists() =>
        logger.info("Loading data from local JSON file")

        loadJsonData[Map[Region, List[(SummonerDTO, MatchlistDto)]]](value)

      case file =>
        logger.info("Estimated time to get the data from the API (with a Personal API key): 30 min")
        saveDataAsJson[Map[Region, List[(SummonerDTO, MatchlistDto)]]](summoners.map {
          case (region, os) =>
            (region, os.map(value => (value, {
              val httpRequest: HttpRequest = HttpRequest(uri = uriProtocol + region + riotMatchlistUri + value.accountId)
                .withHeaders(headers.RawHeader(riotToken._1, riotToken._2))

              Await.result(Http().singleRequest(httpRequest), Duration.Inf) match {
                case httpResponse: HttpResponse if httpResponse.status.intValue() == maxRequestRateAchieve =>
                  logger.info(httpResponse.toString())
                  retryRequest(httpResponse)(httpRequest) match {
                    case retryResponse: HttpResponse if retryResponse.status.isSuccess() =>
                      Await.result(Unmarshal[HttpResponse](retryResponse)
                        .to[MatchlistDto], Duration.Inf)
                    case retryResponse =>
                      retryResponse.discardEntityBytes()
                      logger.error("Server retry was failed")
                      logger.error(retryResponse.toString())
                      MatchlistDto(List(), 0, 0, 0) //Default instance if it fails
                  }

                case httpResponse: HttpResponse if httpResponse.status.isSuccess() =>

                  Await.result(Unmarshal[HttpResponse](httpResponse).to[MatchlistDto], Duration.Inf)

                case e =>
                  e.discardEntityBytes()
                  logger.error(e.toString())
                  logger.error("Server responses was failed")
                  MatchlistDto(List(), 0, 0, 0) //Default instance if it fails
              }
            })))
        })(file)
    }

  def getChallengerMatches(pathFile: String, matchesLists: Map[Region, List[(SummonerDTO, MatchlistDto)]])
                          (implicit actorSystem: ActorSystem): Map[Region, List[MatchDto]] =
    getFile(pathFile) match {
      case file if file.exists() =>
        logger.info("Loading data from local JSON file")

        loadJsonData[Map[Region, List[MatchDto]]](file)

      case file =>
        logger.info("Estimated time to get the data from the API (with a Personal API key): +40 min")

        saveDataAsJson[Map[Region, List[MatchDto]]](
          matchesLists.map {
            case (region, list) => (region, list.flatMap {
              case (_, dto) => dto.matches
            }.distinct.take(400).map((reference: MatchReferenceDto) => {
              val httpRequest: HttpRequest = HttpRequest(uri = uriProtocol + region + riotMatchUri + reference.gameId)
                .withHeaders(headers.RawHeader(riotToken._1, riotToken._2))

              Await.result(Http().singleRequest(httpRequest), Duration.Inf) match {
                case httpResponse: HttpResponse if httpResponse.status.intValue() == maxRequestRateAchieve =>
                  logger.info(httpResponse.toString())
                  retryRequest(httpResponse)(httpRequest) match {
                    case retryResponse: HttpResponse if retryResponse.status.isSuccess() =>
                      Await.result(Unmarshal[HttpResponse](retryResponse)
                        .to[MatchDto], Duration.Inf)

                    case retryResponse =>
                      retryResponse.discardEntityBytes()
                      logger.error("Server retry was failed")
                      logger.error(retryResponse.toString())
                      MatchDto(0, 0, 0, List(), "", "", "", 0, "", List(), List(), 0, 0) //Default instance if it fails
                  }


                case httpResponse: HttpResponse if httpResponse.status.isSuccess() =>
                  Await.result(Unmarshal[HttpResponse](httpResponse).to[MatchDto], Duration.Inf)


                case e =>
                  e.discardEntityBytes()
                  logger.error(e.toString())
                  logger.error("Server responses was failed")
                  MatchDto(0, 0, 0, List(), "", "", "", 0, "", List(), List(), 0, 0) //Default instance if it fails
              }
            }))
          }
        )(file)
    }
}
