package service

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpHeader, HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.scalalogging.Logger
import dto.`match`.{MatchDto, MatchReferenceDto, MatchlistDto}
import dto.player.{LeagueItemDTO, LeagueListDTO, SummonerDTO}
import usefull.FilesManagement._
import usefull.Uris._
import usefull.{MaxRequestException, Region}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

//TODO Bring the API calls here
object Services {
  private val maxRequestRateAchieve = 429

  private val logger = Logger("Service")

  //TODO: Cambiar a Option[T] en lugar de crear instancias por defecto

  //TODO: Funciona pero no es muy robusto respecto a fallos por parte de la API
  def getChallengerPlayers(pathFile: String)()
                          (implicit actorSystem: ActorSystem, ec: ExecutionContext, header: RawHeader): Future[Map[Region, LeagueListDTO]] =
    getFile(pathFile) match {
      case value if value.exists() =>
        logger.info("Loading data from local JSON file")

        loadJsonData[Map[Region, LeagueListDTO]](value) //Importante pasarle el tipo para que sepa cómo serializar
      case value =>
        saveDataAsJson[Map[Region, LeagueListDTO]](getLeagueLists(Region.getAllRegions, header))(value)
    }

  private def getLeagueLists(regions: List[Region], riotHeader: RawHeader)(implicit ac: ActorSystem, ex: ExecutionContext): Future[Map[Region, LeagueListDTO]] =
    Future.foldLeft(
      regions map (reg => getLeagueList(reg, riotHeader) map (l => (reg, l)))
    )(Map[Region, LeagueListDTO]())(_ + _)

  private def getLeagueList(region: Region, riotHeader: RawHeader)(implicit ac: ActorSystem, ex: ExecutionContext): Future[LeagueListDTO] =
    Http().singleRequest(
      HttpRequest(uri = uriProtocol + region.reg + riotChallengerUri).withHeaders(riotHeader)
    ) flatMap (Unmarshal(_).to[LeagueListDTO])

  def getChallengerSummoners(pathFile: String, players: Map[Region, LeagueListDTO])
                            (implicit header: RawHeader, actorSystem: ActorSystem, ex: ExecutionContext): Future[Map[Region, List[SummonerDTO]]] =
    getFile(pathFile) match {
      case value if value.exists() =>
        logger.info("Loading data from local JSON file")
        loadJsonData[Map[Region, List[SummonerDTO]]](value)
      case value =>
        logger.info("Estimated time to get the data from the API (with a Personal API key): 20 min")

        saveDataAsJson[Map[Region, List[SummonerDTO]]](getAllSummoners(players.map(entry => (entry._1, entry._2.entries)), header))(value)
    }

  private def getAllSummoners(players: Map[Region, List[LeagueItemDTO]], header: RawHeader)
                             (implicit ex: ExecutionContext, as: ActorSystem) =
    Future.foldLeft(players map (entry => getSummoners(entry._1, entry._2, header)))(Map[Region, List[SummonerDTO]]())(_ + _)

  private def getSummoners(region: Region, players: List[LeagueItemDTO], header: RawHeader)
                          (implicit ex: ExecutionContext, ac: ActorSystem) =
    Future.foldLeft(
      players map (pl => getSummoner(region, pl.summonerId, header))
    )((region, List[SummonerDTO]()))((r, t) => (r._1, t :: r._2))

  private def getSummoner(region: Region, sumId: String, header: RawHeader)
                         (implicit ac: ActorSystem, ex: ExecutionContext): Future[SummonerDTO] = {
    val futHttpRequest = Http().singleRequest(HttpRequest(uri = uriProtocol + region.reg + riotSummonerUri + sumId)
      .withHeaders(header)) transform {
      case util.Success(value) if value.status.intValue() == maxRequestRateAchieve =>
        value.discardEntityBytes()
        throw MaxRequestException(getTimeToWait(value))
      case e => e
    }
    futHttpRequest recoverWith { case e: MaxRequestException =>
      Thread.sleep(e.timeToWait * 1000 + 50)
      logger.info("We are taking a break for " + e.timeToWait + " seg")
      Http().singleRequest(HttpRequest(uri = uriProtocol + region.reg + riotSummonerUri + sumId)
        .withHeaders(header))
    } flatMap (e => {
      Unmarshal(e).to[SummonerDTO]
    })
  }

  private def getTimeToWait(httpResponse: HttpResponse): Int =
    httpResponse.headers.foldLeft[Option[HttpHeader]](None) {
      case (None, header) if header.is("retry-after") => Some(header)
      case (Some(header), _) => Some(header)
      case _ => None
    } map (_.value().toInt) getOrElse (105)

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
                            (implicit header: RawHeader, actorSystem: ActorSystem): Map[Region, List[(SummonerDTO, MatchlistDto)]] =
    getFile(pathFile) match {
      case value if value.exists() =>
        logger.info("Loading data from local JSON file")

        loadJsonData[Map[Region, List[(SummonerDTO, MatchlistDto)]]](value)

      case file =>
        logger.info("Estimated time to get the data from the API (with a Personal API key): 30 min")
        saveDataAsJson[Map[Region, List[(SummonerDTO, MatchlistDto)]]](summoners.map {
          case (region, os) =>
            (region, os.map(value => (value, {
              val httpRequest: HttpRequest = HttpRequest(uri = uriProtocol + region.reg + riotMatchlistUri + value.accountId)
                .withHeaders(header)

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
                          (implicit header: RawHeader, actorSystem: ActorSystem): Map[Region, List[MatchDto]] =
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
              val httpRequest: HttpRequest = HttpRequest(uri = uriProtocol + region.reg + riotMatchUri + reference.gameId)
                .withHeaders(header)

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
