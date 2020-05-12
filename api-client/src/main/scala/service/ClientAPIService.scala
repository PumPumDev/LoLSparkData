package service

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.IOResult
import akka.stream.scaladsl.{Flow, Keep, RunnableGraph, Sink, Source}
import akka.util.ByteString
import configuration.Configuration._
import dto.RegionDTO
import dto.`match`.MatchlistDto
import dto.player.{LeagueListDTO, SummonerDTO}
import io.circe.generic.auto._
import paths.ModelDataPaths._
import utils.APIManage._
import utils.APIStats
import utils.FilesManagement._
import utils.StreamComponents.deserializeData

import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Future, duration}



object ClientAPIService {

  private val playerAPIStats = APIStats("Player API Stats")
  private val summonerAPIStats = APIStats("Summoner API Stats")
  private val matchlistAPIStats = APIStats("Match Reference API Stats")
  private val matchAPIStats = APIStats("Match API Stats")

  def updateChallengerData(regions: Source[RegionDTO, _], headers: List[RawHeader], outputPath: String, parallelism: Int)
                          (implicit as: ActorSystem): RunnableGraph[Future[_]] = {
    regions
      .mapAsyncUnordered(parallelism)(reg =>
        updateChallengerMatches(headers, outputPath, reg,
          updateChallengerMatchReferences(headers, outputPath, reg,
            updateChallengerSummoners(headers, outputPath, reg,
              updateChallengerPlayers(headers, outputPath, reg))))

      )
      .toMat(Sink.reduce[IOResult]((i1, i2) => IOResult(i1.count + i2.count)))(Keep.right)
  }

  def printAPIStatistics(): Unit = {
    playerAPIStats printStatisticsResult()
    summonerAPIStats printStatisticsResult()
    matchlistAPIStats printStatisticsResult()
    matchAPIStats printStatisticsResult()
  }

  private def updateChallengerPlayers(headers: List[RawHeader], outputPath: String, region: RegionDTO)
                                     (implicit as: ActorSystem): Source[ByteString, NotUsed] = {
    //First we delete the old data
    setUpFile(getPlayerPath(outputPath, region))

    //Now we start making petitions to the API
    getDataFromAPI(getHost(region), challengerPlayerUri, List(headers.head), 30, playerAPIStats)
      .alsoTo(writeData(getPlayerPath(outputPath, region))) // We write the data locally as a collateral action
  }

  //TODO: Ya que los summoners y matchReference no se cargan de local. Podriamos no escribirlos??
  private def updateChallengerSummoners(headers: List[RawHeader], outputPath: String, region: RegionDTO, playerSrc: Source[ByteString, _])
                                       (implicit as: ActorSystem): Source[ByteString, _] = {
    //First we delete the old data
    setUpFile(getSummonerPath(outputPath, region))


    deserializeData[LeagueListDTO](playerSrc)
      .flatMapConcat(league => Source(league.entries.map(challengerSummonerUri + _.summonerId)))
      .throttle(100, FiniteDuration(3, duration.MINUTES) + FiniteDuration(1, duration.SECONDS)) // Slowdown to use the API
      .flatMapConcat(uri => getDataFromAPI(getHost(region), uri, List(headers.head), 30, summonerAPIStats))
      .alsoTo(writeData(getSummonerPath(outputPath, region))) // We write the data locally as a collateral action
  }

  // We load the players data
  private def updateChallengerMatchReferences(headers: List[RawHeader], outputPath: String, region: RegionDTO, sumSrc: Source[ByteString, _])
                                             (implicit as: ActorSystem): Source[ByteString, _] = {
    //First we delete the old data
    setUpFile(getMatchesReferencesPath(outputPath, region))

    deserializeData[SummonerDTO](sumSrc)
      .via(Flow.fromFunction(summoner => challengerMatchlistUri + summoner.accountId))
      .throttle(100, FiniteDuration(3, duration.MINUTES) + FiniteDuration(1, duration.SECONDS)) // Slowdown to use the API
      .flatMapConcat(uri => getDataFromAPI(getHost(region), uri, List(headers.head), 30, matchlistAPIStats))
      .alsoTo(writeData(getMatchesReferencesPath(outputPath, region))) // We write the data locally as a collateral action

  }

  private def updateChallengerMatches(headers: List[RawHeader], outputPath: String, region: RegionDTO, refSrc: Source[ByteString, _])
                                     (implicit as: ActorSystem): Future[IOResult] = {
    //First we delete the old data
    setUpFile(getMatchesPath(outputPath, region))

    //TODO: Make a more declarative solution
    val idsProcessed: mutable.Set[Long] = mutable.Set()

    deserializeData[MatchlistDto](refSrc)
      .flatMapConcat(matchList => Source(matchList.matches))
      .filterNot(matchRef => wasIdProcessed(matchRef.gameId, idsProcessed))
      .via(Flow.fromFunction(challengerMatchUri + _.gameId))
      .throttle(100 * headers.length, FiniteDuration(2, duration.MINUTES) + FiniteDuration(1, duration.SECONDS)) // Slowdown to use the API
      .flatMapConcat(uri => getDataFromAPI(getHost(region), uri, headers, 0, matchAPIStats))
      .toMat(writeData(getMatchesPath(outputPath, region)))(Keep.right).run() // We run the Graph associate to this region
  }

  private def wasIdProcessed(id: Long, idsProcessed: mutable.Set[Long]): Boolean = {
    val idAlreadyProcessed = idsProcessed(id)
    idsProcessed += id
    idAlreadyProcessed
  }

  private def getHost(regionDTO: RegionDTO): String =
    s"$regionDTO.api.riotgames.com"

}
