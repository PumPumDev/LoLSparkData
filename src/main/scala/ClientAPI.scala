import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.Materializer
import com.typesafe.config.{Config, ConfigFactory}
import dto.`match`.{MatchDto, MatchlistDto}
import dto.player.{LeagueListDTO, SummonerDTO}
import json.protocol.JsonCustomProtocol
import service.Services._
import spray.json._
import usefull.Region

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}


object ClientAPI extends JsonCustomProtocol with DefaultJsonProtocol with App {

  //Dependencies
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val config: Config = ConfigFactory.load("credentials.properties")
  /*
    //Spark variables
    //Spark context
    val sparkSession: SparkSession = SparkSession.builder().appName("MainTestApp")
      .master("local[*]").getOrCreate()

    import org.apache.log4j.{Level, Logger}
    Logger.getRootLogger.setLevel(Level.ERROR)*/


  //Path to save/load the data
  val outputPath: String = config.getString("outputPath")
  val challengerPlayersPath: String = outputPath + "challengerPlayersTest.json" //¿Añadir los paths a la configuración o dejarlos aqui?
  val challengerSummonerPath: String = outputPath + "challengerSummonersTest.json"
  val challengerMatchlistPath: String = outputPath + "challengerMatchlist.json"
  val challengerMatchesPath: String = outputPath + "challenger1000Matches.json"

  //API Request Headers
  implicit val riotHeader: RawHeader = RawHeader(config.getString("riotToken"), config.getString("apiKey"))


  val challengerPlayerDS: Map[Region, LeagueListDTO] = Await.result(getChallengerPlayers(challengerPlayersPath), Duration.Inf)

  /*
  Muchas llamadas a la API sería mejor una estrategia concurrente

  Gran problema con el volumen de llamadas a la API = Mucho tiempo en la recopilación de datos
   */
  val challengerSummonerDS: Map[Region, List[SummonerDTO]] =
    Await.result(getChallengerSummoners(challengerSummonerPath, challengerPlayerDS), Duration.Inf)

  val challengerMatchlistDS: Map[Region, List[(SummonerDTO, MatchlistDto)]] =
    getChallengerMatchlist(challengerMatchlistPath, challengerSummonerDS)

  val challengerMatchesDS: Map[Region, List[MatchDto]] =
    getChallengerMatches(challengerMatchesPath, challengerMatchlistDS)

  //We start the Queries
  /*
  Function to measure the time executing a function f
   */
  def execTime[T](f: => T): (T, Long) = {
    val start = System.nanoTime()
    val res = f
    (res, System.nanoTime() - start)
  }

  //sparkSession.read.option("multiline",value = true).json(challengerPlayersPath).select("br1").show()
  println()

  println("END")
  //Obtenemos un "harmless" error al cerrar el cliente TODO Arreglarlo
  //fixme: Parece que al añadir los loggers ya no cierra el cliente al terminar de procesar
  Await.result(system.terminate(), Duration.Inf)


}

