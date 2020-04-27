package service

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.stream.scaladsl.{Flow, Source}
import com.typesafe.scalalogging.Logger
import usefull.Region

import scala.util.{Random, Try}

object StreamComponents {
  private val logger = Logger("Stream Components")

  //TODO improve the header election, not random.
  def createRequests(region: Region, uris: List[String])
                    (implicit headers: List[RawHeader]): Source[(HttpRequest, Uri), NotUsed] =
    Source(uris map (uri => HttpRequest(uri = uri)
      .withHeaders(getRandomHeader(headers)) // Refactor header usage
      -> Uri(uri)))

  def sendRequest(region: Region)
                 (implicit actorSystem: ActorSystem): Flow[(HttpRequest, Uri), (Try[HttpResponse], Uri), Http.HostConnectionPool] =
    Http().cachedHostConnectionPoolHttps[Uri](host = s"$region.api.riotgames.com")

  def createRequestRetry(uris: List[Uri], headers: List[RawHeader]): Source[(HttpRequest, Uri), NotUsed] =
    Source(uris.map(uri => HttpRequest(uri = uri).withHeaders(getRandomHeader(headers)) -> uri))

  private def getRandomHeader(headers: List[RawHeader]) = headers(Random.nextInt(headers.length))

}
