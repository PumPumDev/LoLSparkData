import java.io.{File, PrintWriter}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, headers}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.typesafe.config.{Config, ConfigFactory}
import dto.player.LeagueListDTO
import json.protocol.JsonDtoProtocol
import spray.json._
import usefull.Regions
import usefull.Regions.Region

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.io.Source


object ClientAPI extends JsonDtoProtocol with DefaultJsonProtocol {

  def main(args: Array[String]): Unit = {
    //Initialization
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    val config: Config = ConfigFactory.load("credentials.properties")

    //Pueden aumentarse el número de parámetros que se dejan para la configuracion
    //TODO: Llevarme las Uris a Utilities
    val uriProtocol: String = "https://"
    val riotChallengerUri: String = ".api.riotgames.com/lol/league/v4/challengerleagues/by-queue/RANKED_SOLO_5x5"
    val riotSummonerUri: String = ".api.riotgames.com/lol/summoner/v4/summoners/"
    val riotMatchlistUri: String = ".api.riotgames.com/lol/match/v4/matchlists/by-account/"
    val riotMatchUri: String = ".api.riotgames.com/lol/match/v4/matches"
    val riotToken: (String, String) = (config.getString("riotToken"), config.getString("apiKey"))

    /*
 Este método nos da los primeros datos con los que trabajaremos is bien no aprovecha las posibilidades de la concurrencia
 de peticiones HTTP lo elegimos porque sigue un paradigma más funcional
  */
    val challengerPlayerDS: Map[Region, LeagueListDTO] = Regions.values.map(reg => {
      (reg, Await.result(Unmarshal[HttpResponse](Await.result(Http().singleRequest(HttpRequest(uri = uriProtocol + reg + riotChallengerUri)
        .withHeaders(headers.RawHeader(riotToken._1, riotToken._2))): Future[HttpResponse], Duration.Inf): HttpResponse)
        .to[LeagueListDTO], Duration.Inf): LeagueListDTO) //Da un warning por la fecha, al parecer nos devuelven la fecha con un format distinto
    }).toMap[Region, LeagueListDTO]



    //We got how to Serializer and Deserializer to Json

    //Write Players
    val filePlayers = new File(config.getString("outputPath") + "challengerPlayers.json")
    if (filePlayers.exists) {
      filePlayers.delete()
      println("Players file was overwritten")
    }
    val writerPlayers = new PrintWriter(filePlayers)

    writerPlayers.write(challengerPlayerDS.toJson.prettyPrint)
    writerPlayers.close()

    //Load data Players
    val input = Source.fromFile(config.getString("outputPath") + "challengerPlayers.json")("UTF-8")

    val jsonData: JsValue = input.mkString.parseJson

    input.close()

    val newPlayerDS: Map[Region, LeagueListDTO] = jsonData.convertTo[Map[Region, LeagueListDTO]]

    //TODO Separar las llamadas a distintos métodos que sean llamados desde el cliente
    //TODO Comprobar que el fichero no existe para hacer la llamada si ya existe cargarlo (evitamos llamadas innesarias a la API)
    /*
    Muchas llamadas a la API sería mejor una estrategia concurrente
     */
    /*val challengerSummonerDS: Map[Region, List[SummonerDTO]] = challengerPlayerDS.map(mapEntry =>
      (mapEntry._1, mapEntry._2.entries.map(item =>
        Await.result(
          Unmarshal[HttpResponse](
            Await.result(
              Http().singleRequest(
                HttpRequest(uri = uriProtocol + mapEntry._1 + riotSummonerUri + item.summonerId)
                  .withHeaders(headers.RawHeader(riotToken._1, riotToken._2))), Duration.Inf))
            .to[SummonerDTO], Duration.Inf)): List[SummonerDTO])) //Algunos summoners aparecen vacios al hacer la llamada. Riot Api Fails

    //Write Summoners
    val fileSummoners = new File(config.getString("outputPath") + "challengerSummoner.txt")
    if (fileSummoners.exists) {
      fileSummoners.delete()
      println("Summoners file was overwritten")
    }
    val writerSummoners = new PrintWriter(fileSummoners)
    writerSummoners.write(challengerSummonerDS.toString())
    writerSummoners.close()


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

