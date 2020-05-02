package utils

import java.io.File
import java.nio.file.{NoSuchFileException, Paths, StandardOpenOption}

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
    // If the file doesn't exist it throws a NoSuchFIleException
    checkPathAvailable(path)

    import io.circe.parser._
    FileIO.fromPath(Paths.get(path))
      .via(JsonFraming.objectScanner(100000))
      .via(Flow.fromFunction(_.utf8String))
      .via(Flow.fromFunction(st => parse(st)))
      .divertTo(Sink.foreach(elem => logger.error(s"There was an error during Json parsing with: ${elem.left.get}")), _.isLeft)
      .via(Flow.fromFunction(_.right.get.as[T]))
      .divertTo(Sink.foreach((elem: Result[T]) => logger.error(s"There was an error during Json decoding into case class: ${elem.left.get}")), _.isLeft)
      .via(Flow.fromFunction[Result[T], T](_.right.get))
      .toMat(Sink.collection)(Keep.right).run().map(_.toList)
  }

  // Used to reduce the number of elements to process
  def loadData[T](path: String, maxNum: Int)
                 (implicit decoder: Decoder[T], materializer: Materializer, ex: ExecutionContext): Future[List[T]] = {
    loadData(path).map(_.take(maxNum))
  }

  private def checkPathAvailable(path: String): Unit =
    if (!new File(path).exists())
      throw new NoSuchFileException(s"This file doesn't exist $path")

  def writeData(outputPath: String): Sink[ByteString, Future[IOResult]] =
    FileIO.toPath(Paths.get(outputPath), options = Set(StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND))

  def setUpFile(path: String): Unit = {
    val file: java.io.File = new File(path)
    file.getParentFile.mkdirs
    file.delete
    file.createNewFile()
  }
}
