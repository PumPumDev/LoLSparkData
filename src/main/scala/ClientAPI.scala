import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.Materializer
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger
import service.Services._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}


object ClientAPI extends App {

  private val logger = Logger("API Client")

  //Dependencies
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val config: Config = ConfigFactory.load("credentials.properties")

  //Path to save/load the data
  val outputPath: String = config.getString("outputPath")
  //TODO: Do it better!! Loop or something
  val apiKeysAvailable: List[RawHeader] = List(RawHeader(config.getString("riotToken"), config.getString("apiKey1")),
    RawHeader(config.getString("riotToken"), config.getString("apiKey2")),
    RawHeader(config.getString("riotToken"), config.getString("apiKey3")),
    RawHeader(config.getString("riotToken"), config.getString("apiKey4")),
    RawHeader(config.getString("riotToken"), config.getString("apiKey5")))


  // Is this the correct use of for-comprehension?
  val results = for {
    // Uncomment this lane to update de data from RIOT API
    //_ <- updateChallengerData(Region.getAllRegions, apiKeysAvailable, outputPath)
    playersData <- getChallengerPlayers(outputPath)
    matchesData <- getChallengerMatches(outputPath)
  } yield (playersData, matchesData)


  println(Await.result(results, Duration.Inf))

  logger.info("Data is ready to be used")

  def execTime[T](f: => T): (T, Long) = {
    val start = System.nanoTime()
    val res = f
    (res, System.nanoTime() - start)
  }

  Await.result(Http().shutdownAllConnectionPools(), Duration.Inf)
  println(Await.result(system.terminate, Duration.Inf))

  logger.info("END Processors")

}

