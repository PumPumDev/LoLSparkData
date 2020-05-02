package utils

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpResponse, Uri}
import akka.stream.IOResult
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.ByteString
import com.typesafe.scalalogging.Logger
import utils.FilesManagement._
import utils.StreamComponents.{createRequestRetry, createRequests, sendRequest}

import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future, duration}
import scala.util.{Success, Try}

object APIManage {
  private val logger = Logger("API Manage")

  private val errorResponseCondition: ((Try[HttpResponse], Uri)) => Boolean = {
    case (Success(value), _) if value.status.isSuccess() => false
    case _ => true
  }

  def getDataFromAPI(host: String, uris: List[String], outputPath: String, headers: List[RawHeader])
                    (implicit as: ActorSystem, ex: ExecutionContext): Future[IOResult] = {
    val errorResponses: mutable.MutableList[Uri] = mutable.MutableList()

    //Lanzamos las peticiones
    createRequests(uris, headers).throttle(headers.length * 100, FiniteDuration(3, duration.MINUTES) + FiniteDuration(1, duration.SECONDS))
      .via(sendRequest(host)).divertTo(Sink.foreach(elem => {
      elem._1.getOrElse(HttpResponse()).discardEntityBytes()
      logger.warn(s"Elem failed on 1st try to $host: $elem")
      errorResponses += elem._2
    }), errorResponseCondition)
      .flatMapConcat(poolResponse => poolResponse._1.get.entity.dataBytes.concat(Source.single(ByteString("\n")))) //After each response insert "\n"
      .toMat(writeData(outputPath))(Keep.right).run().transformWith(result => {
      logger.info(s"First Request result of $host: $result")
      errorResponseHandler(errorResponses.toList, host, outputPath, headers)
    })
  }

  //TODO: Podria añadirse los nuevos resultados a la fuente inicial de HttpResponse??
  //TODO: Evitar el uso de variables mutables
  private def errorResponseHandler(errorResponses: List[Uri], host: String, outputPath: String, headers: List[RawHeader])
                                  (implicit as: ActorSystem, ex: ExecutionContext): Future[IOResult] = {

    createRequestRetry(errorResponses, headers).initialDelay(FiniteDuration(2, duration.MINUTES))
      .throttle(100 * headers.length, FiniteDuration(2, duration.MINUTES) + FiniteDuration(1, duration.SECONDS))
      .via(sendRequest(host)).divertTo(Sink.foreach(elem => {
      elem._1.getOrElse(HttpResponse()).discardEntityBytes()
      logger.error(s"Elem failed on retry to $host: $elem")
    }), errorResponseCondition)
      .flatMapConcat(poolResponse => poolResponse._1.get.entity.dataBytes)
      .toMat(writeData(outputPath))(Keep.right).run()
      .transform(result => {
        logger.info(s"Second Request result of $host: $result")
        result
      })
  }
}
