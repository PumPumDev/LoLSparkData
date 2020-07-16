package utils

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.stream.Attributes
import akka.stream.scaladsl.{Flow, JsonFraming, Sink, Source}
import akka.util.ByteString
import com.typesafe.scalalogging.Logger
import io.circe.Decoder
import io.circe.Decoder.Result
import io.circe.parser.parse

import scala.util.{Random, Try}

object StreamComponents {
  private val logger = Logger("Stream Components")

  //TODO improve the header election, not random.
  def createRequest(uri: String, headers: List[RawHeader]): Source[(HttpRequest, Uri), NotUsed] =
    Source.single(HttpRequest(uri = uri)
      .withHeaders(getRandomHeader(headers))
      -> Uri(uri))

  def sendRequest(host: String)
                 (implicit actorSystem: ActorSystem): Flow[(HttpRequest, Uri), (Try[HttpResponse], Uri), Http.HostConnectionPool] =
    Http().cachedHostConnectionPoolHttps[Uri](host = host)

  def createRequestRetry(uri: Uri, headers: List[RawHeader]): Source[(HttpRequest, Uri), NotUsed] =
    Source.single(HttpRequest(uri = uri).withHeaders(getRandomHeader(headers)) -> uri)

  def deserializeData[T](src: Source[ByteString, _])
                        (implicit decoder: Decoder[T]): Source[T, _] = {
    src
      .via(JsonFraming.objectScanner(100000))
      .via(Flow.fromFunction(_.utf8String))
      .via(Flow.fromFunction(st => parse(st)))
      .divertTo(Sink.foreach(elem => logger.error(s"There was an error during Json parsing with: ${elem.left.get}")), _.isLeft)
      .via(Flow.fromFunction(_.right.get.as[T]))
      .divertTo(Sink.foreach((elem: Result[T]) => logger.error(s"There was an error during Json decoding into case class: ${elem.left.get}")), _.isLeft)
      .via(Flow.fromFunction[Result[T], T](_.right.get))
      .log("Deserializer Stream").addAttributes(Attributes.logLevels(onElement = Attributes.LogLevels.Debug))
  }

  private def getRandomHeader(headers: List[RawHeader]) = headers(Random.nextInt(headers.length))

}
