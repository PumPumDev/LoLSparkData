package utils

import java.util.NoSuchElementException

import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpResponse, Uri}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{Attributes, Materializer}
import akka.util.ByteString
import com.typesafe.scalalogging.Logger
import utils.StreamComponents.{createRequest, createRequestRetry, sendRequest}

import scala.concurrent.duration
import scala.concurrent.duration.FiniteDuration
import scala.util.{Success, Try}

object APIManage {
  private val logger = Logger("API Manage")

  def getDataFromAPI(host: String, uri: String, headers: List[RawHeader], delay: Int, stats: APIStats)
                    (implicit as: ActorSystem): Source[ByteString, NotUsed] = {

    createRequest(uri, headers)
      .log("Request Send Stream").addAttributes(Attributes.logLevels(onElement = Attributes.LogLevels.Debug))
      .via(sendRequest(host))
      .log("Response Receive Stream").addAttributes(Attributes.logLevels(onElement = Attributes.LogLevels.Debug))
      .via(processResponse)
      .recoverWithRetries(attempts = 1, errorResponse(uri, host, headers, delay))
      .alsoTo(Sink.fold(stats)((res, el) => res(el))) // Do we need to consume the http response ??
      .flatMapConcat(poolResponse => toSource(poolResponse).concat(Source.single(ByteString("\n")))) //After each response insert "\n"
  }

  private def errorResponse(errorUri: Uri, host: String, headers: List[RawHeader], delay: Int)
                           (implicit as: ActorSystem):
  PartialFunction[Throwable, Source[(Try[HttpResponse], Uri), NotUsed]] = { // There was no error
    case _: NoSuchElementException => //There was an error
      logger.warn(s"We are retrying $errorUri in $host...")
      createRequestRetry(errorUri, headers).initialDelay(FiniteDuration(delay, duration.SECONDS)) //We wait and retry the petition
        .via(sendRequest(host))
        .log("Response Retry Receive Stream").addAttributes(Attributes.logLevels(onElement = Attributes.LogLevels.Warning))
  }

  private def processResponse(implicit mat: Materializer): Flow[(Try[HttpResponse], Uri), (Try[HttpResponse], Uri), NotUsed] =
    Flow.fromFunction {
      case (Success(value: HttpResponse), uri: Uri) if value.status.isSuccess() => (Success(value), uri)
      case serverResponse =>
        serverResponse._1.getOrElse(HttpResponse()).discardEntityBytes()
        logger.warn(s"The serve response was $serverResponse. The request will be retried")
        throw new NoSuchElementException("Server response with an error") // There was an error processing the response, we throw an exception
    }

  private def toSource(serverResponse: (Try[HttpResponse], Uri))
                      (implicit materializer: Materializer): Source[ByteString, _] =
    serverResponse match {
      case (Success(value), _) if value.status.isSuccess() => value.entity.dataBytes
      case (Success(value), _) if value.status.isFailure() =>
        value.discardEntityBytes()
        HttpResponse().entity.dataBytes
      case _ => HttpResponse().entity.dataBytes
    }
}
