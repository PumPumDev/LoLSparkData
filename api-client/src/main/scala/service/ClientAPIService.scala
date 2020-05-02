package service

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.IOResult
import com.typesafe.scalalogging.Logger
import configuration.Configuration._
import dto.RegionDTO
import dto.`match`.MatchlistDto
import dto.player.{LeagueListDTO, SummonerDTO}
import io.circe.generic.auto._
import paths.ModelDataPaths._
import utils.APIManage._
import utils.FilesManagement._

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


//TODO: Use loggers in streams
object ClientAPIService {

  private val logger = Logger("Client API Service")

  //TODO: Hacer mejor los loggers y devoluciones en caso de error
  def updateChallengerData(regions: List[RegionDTO], headers: List[RawHeader], outputPath: String)
                          (implicit as: ActorSystem, ex: ExecutionContext): Future[List[IOResult]] =
    Future.foldLeft(regions map (reg => {
      logger.info(s"Updating $reg players data...")
      updateChallengerPlayers(headers, outputPath, reg).transformWith[IOResult] {
        case Success(_) =>
          logger.info(s"$reg Players was successfully updated")
          logger.info(s"Updating $reg summoners data...")
          updateChallengerSummoners(headers, outputPath, reg).transformWith[IOResult] {
            case Success(_) =>
              logger.info(s"$reg Summoners was successfully updated")
              logger.info(s"Updating $reg match references data...")
              updateChallengerMatchReferences(headers, outputPath, reg).transformWith[IOResult] {
                case Success(_) =>
                  logger.info(s"$reg Match references was successfully updated")
                  logger.info(s"Updating $reg matches data...")
                  updateChallengerMatches(headers, outputPath, reg).transform {
                    case Success(value: IOResult) =>
                      logger.info(s"$reg Matches was successfully updated")
                      logger.info(s"Region $reg data was successfully updated !!")
                      Try(value)
                    case Failure(exception) =>
                      logger.error(s"There was an error updating challenger $reg MATCHES data\n$exception")
                      Try(IOResult(0))
                  }
                case Failure(exception) =>
                  logger.error(s"There was an error updating challenger $reg MATCH REFERENCES data\n$exception")
                  Future(IOResult(0))
              }
            case Failure(exception) =>
              logger.error(s"There was an error updating challenger $reg SUMMONERS data\n$exception")
              Future(IOResult(0))
          }
        case Failure(exception) =>
          logger.error(s"There was an error updating challenger $reg PLAYERS data\n$exception")
          Future(IOResult(0))
      }
    }))(List[IOResult]())((result, elem) => elem :: result)


  private def updateChallengerPlayers(headers: List[RawHeader], outputPath: String, region: RegionDTO)
                                     (implicit as: ActorSystem, ex: ExecutionContext): Future[IOResult] = {
    //First we delete the old data
    setUpFile(getPlayerPath(outputPath, region))

    //Now we start making petitions to the API
    getDataFromAPI(getHost(region), List(challengerPlayerUri), getPlayerPath(outputPath, region), List(headers.head))
  }

  private def updateChallengerSummoners(headers: List[RawHeader], outputPath: String, region: RegionDTO)
                                       (implicit as: ActorSystem, ex: ExecutionContext): Future[IOResult] = {
    //First we delete the old data
    setUpFile(getSummonerPath(outputPath, region))

    // We load the players data
    loadData[LeagueListDTO](getPlayerPath(outputPath, region))
      .map(_.flatMap(_.entries.map(item => challengerSummonerUri + item.summonerId)))
      .flatMap(uris => getDataFromAPI(getHost(region), uris, getSummonerPath(outputPath, region), List(headers.head))) //Then we make de API calls

  }

  // We load the players data
  private def updateChallengerMatchReferences(headers: List[RawHeader], outputPath: String, region: RegionDTO)
                                             (implicit as: ActorSystem, ex: ExecutionContext): Future[IOResult] = {
    //First we delete the old data
    setUpFile(getMatchesReferencesPath(outputPath, region))

    // We load the summoners data
    loadData[SummonerDTO](getSummonerPath(outputPath, region)).map(_.map(challengerMatchlistUri + _.accountId))
      .flatMap(uris =>
        getDataFromAPI(getHost(region), uris, getMatchesReferencesPath(outputPath, region), List(headers.head))) //Then we make de API calls

  }

  private def updateChallengerMatches(headers: List[RawHeader], outputPath: String, region: RegionDTO)
                                     (implicit as: ActorSystem, ex: ExecutionContext): Future[IOResult] = {
    //First we delete the old data
    setUpFile(getMatchesPath(outputPath, region))

    //TODO: Make a more declarative solution
    val idsProcessed: mutable.Set[Long] = mutable.Set()

    // We load the match references data
    loadData[MatchlistDto](getMatchesReferencesPath(outputPath, region))
      .map(_.flatMap(_.matches).filterNot(ref => wasIdProcessed(ref.gameId, idsProcessed)).map(challengerMatch + _.gameId))
      .flatMap(uris => getDataFromAPI(getHost(region), uris, getMatchesPath(outputPath, region), headers))
  }

  private def wasIdProcessed(id: Long, idsProcessed: mutable.Set[Long]): Boolean = {
    val idAlreadyProcessed = idsProcessed(id)
    idsProcessed += id
    idAlreadyProcessed
  }

  private def getHost(regionDTO: RegionDTO): String =
    s"$regionDTO.api.riotgames.com"

}
