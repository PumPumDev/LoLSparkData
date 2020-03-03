import dto.`match`.{MatchDto, MatchlistDto}
import dto.player.{LeagueListDTO, SummonerDTO}
import json.protocol.JsonCustomProtocol
import service.Services._
import spray.json._
import usefull.LoadObject._
import usefull.Regions.Region

import scala.concurrent.Await
import scala.concurrent.duration.Duration


object ClientAPI extends JsonCustomProtocol with DefaultJsonProtocol {

  def main(args: Array[String]): Unit = {

    val challengerPlayerDS: Map[Region, LeagueListDTO] = getChallengerPlayers

    /*
    Muchas llamadas a la API sería mejor una estrategia concurrente

    Gran problema con el volumen de llamadas a la API
    La solución será reducir el tamaño de la muestra a estudiar
     */
    val challengerSummonerDS: Map[Region, List[SummonerDTO]] = getChallengerSummoners(challengerPlayerDS)

    val challengerMatchlistDS: Map[Region, List[(SummonerDTO, MatchlistDto)]] = getChallengerMatchlist(challengerSummonerDS)

    val challengerMatches: Map[Region, List[MatchDto]] = getChallengerMatches(challengerMatchlistDS)

    //Obtenemos un "harmless" error al cerrar el cliente TODO Arreglarlo
    Await.result(system.terminate(), Duration.Inf)
  }

}

