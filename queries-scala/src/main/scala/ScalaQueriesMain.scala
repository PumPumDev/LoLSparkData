
import akka.actor.ActorSystem
import com.typesafe.scalalogging.Logger
import configuration.Configuration._
import services.ScalaQueriesService.{getChallengerMatches, getChallengerPlayers}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}

object ScalaQueriesMain extends App {

  private val logger: Logger = Logger("Scala Queries Main")

  //Implicit variables
  implicit val system: ActorSystem = ActorSystem("Scala_Queries_Actor_System")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  logger.info("Starting the data loading... (should not take much time)")

  // Parallel data loading
  val playersFut = getChallengerPlayers(outputPath)
  val matchesFut = getChallengerMatches(outputPath)

  val result = for {
    players <- playersFut
    matches <- matchesFut
  } yield (players, matches)

  println(Await.result(result, Duration.Inf))

  println(Await.result(system.terminate(), Duration.Inf))
}
