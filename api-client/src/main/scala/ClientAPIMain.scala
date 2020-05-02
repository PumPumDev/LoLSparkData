import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.scalalogging.Logger
import configuration.Configuration._
import dto.RegionDTO
import service.ClientAPIService.updateChallengerData

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}

object ClientAPIMain extends App {

  private val logger = Logger("API Client")

  //Implicit variables
  implicit val system: ActorSystem = ActorSystem("API_Client_Actor_System")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  logger.info("Data will be downloaded. This will take some time (01:30 h approx)")

  Await.result(updateChallengerData(RegionDTO.getAllRegions, headers, outputPath), Duration.Inf)

  Await.result(Http().shutdownAllConnectionPools(), Duration.Inf)
  logger.info(Await.result(system.terminate, Duration.Inf).toString)

  logger.info("Data is updated and ready to be used")

}
