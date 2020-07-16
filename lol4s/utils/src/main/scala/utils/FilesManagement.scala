package utils

import java.io.File
import java.nio.file.{NoSuchFileException, Paths, StandardOpenOption}

import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Sink, Source}
import akka.util.ByteString

import scala.concurrent.Future

object FilesManagement {

  def loadData(path: String): Source[ByteString, Future[IOResult]] = {
    // If the file doesn't exist it throws a NoSuchFIleException
    checkPathAvailable(path)

    FileIO.fromPath(Paths.get(path))
  }

  private def checkPathAvailable(path: String): Unit =
    if (!new File(path).exists())
      throw new NoSuchFileException(s"This file doesn't exist $path")

  def writeData(outputPath: String): Sink[ByteString, Future[IOResult]] =
    FileIO.toPath(Paths.get(outputPath), options = Set(StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND))

  def setUpFile(path: String): Unit = {
    val file: java.io.File = new File(path)
    if (file.getParentFile != null)
      file.getParentFile.mkdirs
    file.delete
    file.createNewFile()
  }
}
