import java.util.concurrent.Executors

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, headers}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import dto.{LeagueItemDTO, LeagueListDTO, MiniSeriesDTO, SummonerDTO}
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.RootJsonFormat

import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}
import usefull.UsefulItems.regions

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object ClientAPI {

  def main(args: Array[String]): Unit = {
    val dataSet: List[LeagueListDTO] = List()
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    val key: String = "****************"
    val uriProtocol: String = "https://"
    val riotUri: String = ".api.riotgames.com/lol/league/v4/challengerleagues/by-queue/RANKED_SOLO_5x5"
    val riotToken: (String, String) = ("X-Riot-Token", key)
    /*val responseFuture : Future[HttpResponse] = Http().singleRequest(HttpRequest(
      uri = ".api.riotgames.com/lol/league/v4/challengerleagues/by-queue/RANKED_SOLO_5x5")
      .withHeaders(headers.RawHeader("X-Riot-Token",key)))*/

    implicit val summonerProtocol: RootJsonFormat[SummonerDTO] = jsonFormat7(SummonerDTO)
    implicit val miniSeriesDTO: RootJsonFormat[MiniSeriesDTO] = jsonFormat4(MiniSeriesDTO)
    implicit val leagueItemDTO: RootJsonFormat[LeagueItemDTO] = jsonFormat10(LeagueItemDTO)
    implicit val leagueListDTO: RootJsonFormat[LeagueListDTO] = jsonFormat5(LeagueListDTO)

    /*responseFuture
      .onComplete{
        case Success(value) => print(Unmarshal(value).to[SummonerDTO])
        case Failure(e: Throwable) => sys.error(e.toString)
      }*/

    /*Http().singleRequest(HttpRequest(uri = uriProtocol + regions().head + riotUri)
      .withHeaders(headers.RawHeader(riotToken._1, riotToken._2)))
      .onComplete{
        case Success(value) => println(Unmarshal(value).to[LeagueListDTO])
        case _ => println("Al carrer")
      }*/

    regions().foreach {
      reg =>
        Http().singleRequest(HttpRequest(uri = uriProtocol + reg + riotUri)
          .withHeaders(headers.RawHeader(riotToken._1, riotToken._2)))
          .onComplete {
            case Success(value) => {
              println(Unmarshal(value).to[LeagueListDTO])
            }
            case _ => println("Fucked")
          }
    }
  }
}
