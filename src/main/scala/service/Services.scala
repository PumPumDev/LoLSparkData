package service

import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpResponse, Uri}
import akka.stream.scaladsl.{Flow, Keep, Sink}
import akka.stream.{IOResult, Materializer}
import com.typesafe.scalalogging.Logger
import dto.`match`.{MatchDto, MatchlistDto}
import dto.player.{LeagueListDTO, SummonerDTO}
import service.StreamComponents._
import usefull.FilesManagement._
import usefull.Region
import usefull.Uris._

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future, duration}
import scala.util.{Failure, Success, Try}


//TODO: Use loggers in streams
object Services { //TODO: File Manage take out of this file

  private val logger = Logger("Service")

  private val errorResponseCondition: ((Try[HttpResponse], Uri)) => Boolean = {
    case (Success(value), _) if value.status.isSuccess() => false
    case _ => true
  }

  //TODO: Hacer mejor los loggers y devoluciones en caso de error
  def updateChallengerData(regions: List[Region], headers: List[RawHeader], outputPath: String)
                          (implicit as: ActorSystem, ex: ExecutionContext): Future[List[IOResult]] =
    Future.foldLeft(regions map (reg => {
      logger.info("Updating players data...")
      updateChallengerPlayers(headers, outputPath, reg).transformWith[IOResult] {
        case Success(_) =>
          logger.info("Players was successfully updated")
          logger.info("Updating summoners data...")
          updateChallengerSummoners(headers, outputPath, reg).transformWith[IOResult] {
            case Success(_) =>
              logger.info("Summoners was successfully updated")
              logger.info("Updating match references data...")
              updateChallengerMatchReferences(headers, outputPath, reg).transformWith[IOResult] {
                case Success(_) =>
                  logger.info("Match references was successfully updated")
                  logger.info("Updating matches data...")
                  updateChallengerMatches(headers, outputPath, reg).transform {
                    case Success(value: IOResult) =>
                      logger.info("Matches was successfully updated")
                      logger.info(s"Region $reg data was successfully updated !!")
                      Try(value)
                    case Failure(exception) =>
                      logger.error(s"There was an error updating challenger MATCHES data\n$exception")
                      Try(IOResult(0))
                  }
                case Failure(exception) =>
                  logger.error(s"There was an error updating challenger MATCH REFERENCES data\n$exception")
                  Future(IOResult(0))
              }
            case Failure(exception) =>
              logger.error(s"There was an error updating challenger SUMMONERS data\n$exception")
              Future(IOResult(0))
          }
        case Failure(exception) =>
          logger.error(s"There was an error updating challenger PLAYERS data\n$exception")
          Future(IOResult(0))
      }
    }))(List[IOResult]())((result, elem) => elem :: result)


  def getChallengerPlayers(outputPath: String)(implicit mat: Materializer, ex: ExecutionContext): Future[Map[Region, LeagueListDTO]] = {
    //TODO: Refactor Paths and what happen if the file does not exist
    Future.foldLeft(Region.getAllRegions.map(reg => loadData[LeagueListDTO](getPlayerPath(outputPath, reg)).map(reg -> _.head)))(Map[Region, LeagueListDTO]())(_ + _)
  }

  //TODO: Return unique Future
  //FIXME: If the directory does not exist it the IO operation fails
  def updateChallengerPlayers(headers: List[RawHeader], outputPath: String, region: Region)
                             (implicit as: ActorSystem, ex: ExecutionContext): Future[IOResult] = {
    //First we delete the old data
    new File(getPlayerPath(outputPath, region)).delete()

    //Now we start making petitions to the API
    getDataFromAPI(region, List(riotChallengerUri), getPlayerPath(outputPath, region), headers)
  }

  def updateChallengerSummoners(headers: List[RawHeader], outputPath: String, region: Region)
                               (implicit as: ActorSystem, ex: ExecutionContext): Future[IOResult] = {
    //First we delete the old data
    new File(getSummonerPath(outputPath, region)).delete()

    // We load the players data TODO: Check the data is available and get if it is not
    loadData[LeagueListDTO](getPlayerPath(outputPath, region))
      .map(_.flatMap(_.entries.map(item => riotSummonerUri + item.summonerId)))
      .flatMap(uris => getDataFromAPI(region, uris, getSummonerPath(outputPath, region), headers)) //Then we make de API calls
  }

  // We load the players data TODO: Check the data is available and get if it is not
  def updateChallengerMatchReferences(headers: List[RawHeader], outputPath: String, region: Region)
                                     (implicit as: ActorSystem, ex: ExecutionContext): Future[IOResult] = {
    //First we delete the old data
    new File(getMatchesReferencesPath(outputPath, region)).delete()

    // We load the summoners data TODO: Check the data is available and get if it is not
    loadData[SummonerDTO](getSummonerPath(outputPath, region)).map(_.map(riotMatchlistUri + _.accountId))
      .flatMap(uris =>
        getDataFromAPI(region, uris, getMatchesReferencesPath(outputPath, region), headers)) //Then we make de API calls
  }

  def getChallengerMatches(outputPath: String)(implicit mat: Materializer, ex: ExecutionContext): Future[Map[Region, List[MatchDto]]] = {
    Future.foldLeft(Region.getAllRegions.map(reg => loadData[MatchDto](getMatchesPath(outputPath, reg)).map(reg -> _)))(Map[Region, List[MatchDto]]())(_ + _)
  }

  def updateChallengerMatches(headers: List[RawHeader], outputPath: String, region: Region)
                             (implicit as: ActorSystem, ex: ExecutionContext): Future[IOResult] = {
    //First we delete the old data
    new File(getMatchesPath(outputPath, region)).delete()

    // We load the match references data
    loadData[MatchlistDto](getMatchesReferencesPath(outputPath, region))
      .map(_.flatMap(_.matches).map(riotMatchUri + _.gameId)) //TODO: IF there are too many matches we can take just first 1000 or something like that
      .flatMap(uris => getDataFromAPI(region, uris, getMatchesPath(outputPath, region), headers))
  }


  //TODO: Use implicit headers
  //Hay un caso extremo que es que TODAS las petiones fallen. Que genera una excepcion por la concatenación de Source vacias
  def getDataFromAPI(region: Region, uris: List[String], outputPath: String, headers: List[RawHeader])
                    (implicit as: ActorSystem, ex: ExecutionContext): Future[IOResult] = {
    var errorResponses: List[Uri] = List()

    //Lanzamos las peticiones
    createRequests(region, uris)(headers).throttle(100, FiniteDuration(2, duration.MINUTES) + FiniteDuration(1, duration.SECONDS)) //.log("HTTPCreated: ").addAttributes(Attributes.logLevels(onElement = Attributes.LogLevels.Info))
      .via(sendRequest(region)).divertTo(Sink.foreach(elem => {
      logger.warn(s"Elem failed on 1st try: $elem")
      elem._1.getOrElse(HttpResponse()).discardEntityBytes()
      errorResponses = elem._2 :: errorResponses
    }), errorResponseCondition)
      .via(Flow.fromFunction(_._1.get))
      .flatMapConcat(_.entity.dataBytes)
      .toMat(writeData(region, outputPath))(Keep.right).run().transformWith(result => {
      logger.info(s"First Request result: $result")
      errorResponseHandler(errorResponses, region, outputPath, headers)
    })
  }

  //TODO: Podria añadirse los nuevos resultados a la fuente inicial de HttpResponse??
  //TODO: Evitar el uso de variables mutables
  private def errorResponseHandler(errorResponses: List[Uri], region: Region, outputPath: String, headers: List[RawHeader])
                                  (implicit as: ActorSystem, ex: ExecutionContext): Future[IOResult] = {


    createRequestRetry(errorResponses, headers).throttle(100, FiniteDuration(2, duration.MINUTES) + FiniteDuration(1, duration.SECONDS))
      .via(sendRequest(region)).divertTo(Sink.foreach(elem => {
      logger.error(s"Elem failed on retry: $elem")
      elem._1.getOrElse(HttpResponse()).discardEntityBytes()
    }), errorResponseCondition).via(Flow.fromFunction(_._1.get)).flatMapConcat(_.entity.dataBytes).toMat(writeData(region, outputPath))(Keep.right).run()
  }

}
