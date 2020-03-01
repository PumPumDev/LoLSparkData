import dto.`match`.MatchlistDto
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

    //TODO Separar las llamadas a distintos métodos que sean llamados desde el cliente
    //TODO Comprobar que el fichero no existe para hacer la llamada si ya existe cargarlo (evitamos llamadas innesarias a la API)
    /*
    Muchas llamadas a la API sería mejor una estrategia concurrente

    Gran problema con el volumen de llamadas a la API
    La solución será reducir el tamaño de la muestra a estudiar
     */
    val challengerSummonerDS: Map[Region, List[SummonerDTO]] = getChallengerSummoners(challengerPlayerDS)

    val challengerMatchlistDS: Map[Region, List[(SummonerDTO, MatchlistDto)]] = getChallengerMatchlist(challengerSummonerDS)


    /*
      //Conseguimos los MatchReferences por region
      val matchReferencesDS: Map[Region, List[MatchlistDto]] = challengerSummonerDS.map(mapEntry =>
        (mapEntry._1, mapEntry._2.map {
          case item: SummonerDTO if item.accountId.isDefined =>
          Some(Await.result(
            Unmarshal[HttpResponse](
              Await.result(
                Http().singleRequest(
                  HttpRequest(uri = uriProtocol + mapEntry._1 + riotMatchlistUri + item.accountId)
                    .withHeaders(headers.RawHeader(riotToken._1, riotToken._2))), Duration.Inf))
              .to[MatchlistDto], Duration.Inf))
          case item: SummonerDTO if item.accountId.isEmpty => None
        }.filter(_.isDefined).map(_.get)))

      //Write Matchlist
      val fileMatchlist = new File(config.getString("outputPath") + "matchlist.txt")
      if (fileMatchlist.exists) {
        fileMatchlist.delete()
        println("Matchlist file was overwritten")
      }
      val writerMatchlist = new PrintWriter(fileMatchlist)
      writerMatchlist.write(matchReferencesDS.toString())
      writerMatchlist.close()

      //Conseguimos los Matches por región a través de sus referencias
      val matchesDS: Map[Region, List[MatchDto]] = matchReferencesDS.map {
        case (region, listMatchlist) =>
          (region, listMatchlist.map {
            case value: MatchlistDto if value.matches.isDefined =>
              Some(value.matches.get.map(item =>
                Await.result(
                  Unmarshal[HttpResponse](
                    Await.result(
                      Http().singleRequest(
                        HttpRequest(uri = uriProtocol + region + riotMatchUri + item.gameId)
                          .withHeaders(headers.RawHeader(riotToken._1, riotToken._2))), Duration.Inf))
                    .to[MatchDto], Duration.Inf)))
            case value: MatchlistDto if value.matches.isEmpty => None
          }.filter(_.isDefined).flatMap(_.get))
      }

      //Write Matches
      val fileMatches = new File(config.getString("outputPath") + "maches.txt")
      if (fileMatches.exists) {
        fileMatches.delete()
        println("Matches file was overwritten")
      }
      val writerMatches = new PrintWriter(fileMatches)
      writerMatches.write(matchesDS.toString())
      writerMatches.close()*/


    //Volcamos los datos a un fichero //TODO: Hacer codigo de impresión mas limpio


    //Obtenemos un "harmless" error al cerrar el cliente TODO Arreglarlo
    Await.result(system.terminate(), Duration.Inf)
    //Await.result(Http().shutdownAllConnectionPools(),Duration.Inf)
  }

}

