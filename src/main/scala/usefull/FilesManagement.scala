package usefull

import java.nio.file.{Paths, StandardOpenOption}

import akka.stream.scaladsl.{FileIO, Flow, JsonFraming, Keep, Sink}
import akka.stream.{IOResult, Materializer}
import akka.util.ByteString
import com.typesafe.scalalogging.Logger
import io.circe.Decoder
import io.circe.Decoder.Result

import scala.concurrent.{ExecutionContext, Future}


object FilesManagement {

  private val logger = Logger("IOFile")


  //TODO: ADD parameter to explicit the maximumLengthObject
  def loadData[T](path: String)
                 (implicit decoder: Decoder[T], materializer: Materializer, ex: ExecutionContext): Future[List[T]] = {
    import io.circe.parser._

    //Que pasa si el path no existe
    FileIO.fromPath(Paths.get(path))
      .via(JsonFraming.objectScanner(100000))
      .via(Flow.fromFunction(_.utf8String))
      .via(Flow.fromFunction(st => parse(st))).take(100) //Java can't load all the data
      .divertTo(Sink.foreach(elem => logger.error(s"There was an error during Json parsing with: ${elem.left.get}")), _.isLeft)
      .via(Flow.fromFunction(_.right.get.as[T]))
      .divertTo(Sink.foreach((elem: Result[T]) => logger.error(s"There was an error during Json decoding into case class: ${elem.left.get}")), _.isLeft)
      .via(Flow.fromFunction[Result[T], T](_.right.get))
      .toMat(Sink.collection)(Keep.right).run().map(_.toList)
  }

  def writeData(region: Region, outputPath: String): Sink[ByteString, Future[IOResult]] =
    FileIO.toPath(Paths.get(outputPath), options = Set(StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND))

  def getPlayerPath(outputPath: String, region: Region): String =
    s"$outputPath/players/$region.data"

  def getSummonerPath(outputPath: String, region: Region): String =
    s"$outputPath/summoners/$region.data"

  def getMatchesReferencesPath(outputPath: String, region: Region) =
    s"$outputPath/match_references/$region.data"

  def getMatchesPath(outputPath: String, region: Region): String =
    s"$outputPath/fast_matches/$region.data"

}
