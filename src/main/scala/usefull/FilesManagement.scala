package usefull

import java.nio.file.{Paths, StandardOpenOption}

import akka.stream.scaladsl.{FileIO, Flow, JsonFraming, Keep, Sink}
import akka.stream.{IOResult, Materializer}
import akka.util.ByteString
import com.typesafe.scalalogging.Logger
import json.protocol.JsonCustomProtocol
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

/*
Elegimos reducir a este archivo el manejo de ficheros que emplea Java y una programación menos funcional
 */
object FilesManagement extends JsonCustomProtocol {

  private val logger = Logger("IOFile")


  //TODO: ADD parameter to explicit the maximumLengthObject
  def loadData[T](path: String)
                 (implicit reader: JsonReader[T], materializer: Materializer, ex: ExecutionContext): Future[List[T]] = {
    import spray.json._

    //Que pasa si el path no existe
    FileIO.fromPath(Paths.get(path))
      .via(JsonFraming.objectScanner(100000))
      .via(Flow.fromFunction(_.utf8String))
      .via(Flow.fromFunction(_.parseJson.convertTo[T])).toMat(Sink.collection)(Keep.right).run().map(_.toList)
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
    s"$outputPath/matches/$region.data"

}
