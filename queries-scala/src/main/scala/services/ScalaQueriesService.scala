package services

import java.nio.file.NoSuchFileException

import akka.stream.Materializer
import com.typesafe.scalalogging.Logger
import dto.RegionDTO
import dto.`match`.MatchDto
import dto.player.LeagueListDTO
import io.circe.generic.auto._
import paths.ModelDataPaths._
import utils.FilesManagement.loadData

import scala.concurrent.{ExecutionContext, Future}


object ScalaQueriesService {
  private val logger = Logger("Scala Queries Service")

  private val MAX_ELEMS: Integer = 100

  def getChallengerPlayers(outputPath: String)(implicit mat: Materializer, ex: ExecutionContext):
  Future[Either[Exception, Map[RegionDTO, LeagueListDTO]]] = {
    try {
      Future.foldLeft(RegionDTO.getAllRegions.map(reg => loadData[LeagueListDTO](getPlayerPath(outputPath, reg), MAX_ELEMS).map(reg ->
        _.head)))(Right(Map[RegionDTO, LeagueListDTO]()))((either, ele) => Right(either.right.get + ele))
    }
    catch {
      case e: NoSuchFileException =>
        logger.error(s"Player data couldn't be found. Update your data if is not locally storage\nException: ${e.toString}")
        Future(Left(e))
    }
  }

  def getChallengerMatches(outputPath: String)(implicit mat: Materializer, ex: ExecutionContext):
  Future[Either[Exception, Map[RegionDTO, List[MatchDto]]]] = {
    try {
      Future.foldLeft(RegionDTO.getAllRegions.map(reg => loadData[MatchDto](getMatchesPath(outputPath, reg), MAX_ELEMS).map(reg ->
        _)))(Right(Map[RegionDTO, List[MatchDto]]()))((either, ele) => Right(either.right.get + ele))
    }
    catch {
      case e: NoSuchFileException =>
        logger.error(s"Matches data couldn't be found. Update your data if is not locally storage\nException: ${e.toString}")
        Future(Left(e))
    }
  }
}
