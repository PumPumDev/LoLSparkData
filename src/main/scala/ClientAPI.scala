import akka.actor.ActorSystem
import akka.stream.Materializer
import com.typesafe.config.{Config, ConfigFactory}
import dto.`match`.{MatchDto, MatchlistDto}
import dto.player.{LeagueListDTO, SummonerDTO}
import json.protocol.JsonCustomProtocol
import service.Services._
import spray.json._
import usefull.Regions.Region

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}


object ClientAPI extends JsonCustomProtocol with DefaultJsonProtocol with App {

  //Dependencies
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val config: Config = ConfigFactory.load("credentials.properties")

  //Path to save/load the data
  val outputPath: String = config.getString("outputPath")
  val challengerPlayersPath: String = outputPath + "challengerPlayers.json" //¿Añadir los paths a la configuración o dejarlos aqui?
  val challengerSummonerPath: String = outputPath + "challengerSummoners.json"
  val challengerMatchlistPath: String = outputPath + "challengerMatchlist.json"
  val challengerMatchesPath: String = outputPath + "challenger1000Matches.json"


  val challengerPlayerDS: Map[Region, LeagueListDTO] = getChallengerPlayers(challengerPlayersPath)

  /*
  Muchas llamadas a la API sería mejor una estrategia concurrente

  Gran problema con el volumen de llamadas a la API = Mucho tiempo en la recopilación de datos
   */
  val challengerSummonerDS: Map[Region, List[SummonerDTO]] =
    getChallengerSummoners(challengerSummonerPath, challengerPlayerDS)

  val challengerMatchlistDS: Map[Region, List[(SummonerDTO, MatchlistDto)]] =
    getChallengerMatchlist(challengerMatchlistPath, challengerSummonerDS)

  val challengerMatches: Map[Region, List[MatchDto]] =
    getChallengerMatches(challengerMatchesPath, challengerMatchlistDS)

  //Obtenemos un "harmless" error al cerrar el cliente TODO Arreglarlo
  //fixme: Parece que al añadir los loggers ya no cierra el cliente al terminar de procesar
  Await.result(system.terminate(), Duration.Inf)


}

