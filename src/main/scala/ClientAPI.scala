import java.io.PrintWriter

import akka.actor.ActorSystem
import akka.http.scaladsl.{Http, model}
import akka.http.scaladsl.model.{DateTime, HttpRequest, HttpResponse, headers}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.typesafe.config.{Config, ConfigFactory}
import dto.LeagueListDTO
import json.protocol.JsonDtoProtocol
import usefull.UsefulItems.regions

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.reflect.io
import scala.util.Success


object ClientAPI extends JsonDtoProtocol {

  def main(args: Array[String]): Unit = {
    //Initialization
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = Materializer(system)
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    val config: Config = ConfigFactory.load("credentials.properties")

    //Pueden aumentarse el número de parámetros que se dejan para la configuracion
    val uriProtocol: String = "https://"
    val riotUri: String = ".api.riotgames.com/lol/league/v4/challengerleagues/by-queue/RANKED_SOLO_5x5"
    val riotToken: (String, String) = (config.getString("riotToken"), config.getString("apiKey"))

    /*
    Este método nos da los primeros datos con los que trabajaremos is bien no aprovecha las posibilidades de la concurrencia
    de peticiones HTTP lo elegimos porque sigue un paradigma más funcional
     */
    val dataSet: List[LeagueListDTO] = regions().map(reg => {
      Await.result(Unmarshal[HttpResponse](Await.result(Http().singleRequest(HttpRequest(uri = uriProtocol + reg + riotUri)
        .withHeaders(headers.RawHeader(riotToken._1, riotToken._2))): Future[HttpResponse], Duration.Inf): HttpResponse)
        .to[LeagueListDTO], Duration.Inf): LeagueListDTO //Da un warning por la fecha, al parecer nos devuelven la fecha con un format distinto
    })

    //Volcamos los datos a un fichero
    val file = io.File(config.getString("outputPath"))
    if (file.exists) {
      file.delete()
      println("File was overwritten")
    }
    val writer = new PrintWriter(new java.io.File(file.name))

    writer.write(dataSet.toString())
    writer.close()

    //Obtenemos un "harmless" error al cerrar el cliente TODO Arreglarlo
    Await.result(system.terminate(), Duration.Inf)
    //Await.result(Http().shutdownAllConnectionPools(),Duration.Inf)
  }

}

