import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.scalalogging.Logger
import configuration.Configuration._
import dto.RegionDTO
import service.ClientAPIService.{printAPIStatistics, updateChallengerData}
import utils.TimeMeasure

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

object ClientAPIMain extends App {

  private val logger = Logger("API Client")

  //Implicit variables
  implicit val system: ActorSystem = ActorSystem("API_Client_Actor_System")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  logger.info("Data will be downloaded. This will take some time (01:30 h approx)")


  val uniqueFut = Future.sequence(updateChallengerData(RegionDTO.getAllRegions, headers, outputPath).map(_.run()))
  val timeExpend = TimeMeasure(
    Await.result(uniqueFut, Duration.Inf)
  )._2

  if (printApiStats) {
    printAPIStatistics()
    logger.info(s"Total time expended downloading Data: ${timeExpend / (1E9 * 60)} (min)")
  }

  Await.result(Http().shutdownAllConnectionPools(), Duration.Inf)
  logger.info(Await.result(system.terminate, Duration.Inf).toString)

  logger.info("Data is updated and ready to be used")

}
