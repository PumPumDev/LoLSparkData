package usefull

import java.io.{File, PrintWriter}

import com.typesafe.scalalogging.Logger
import json.protocol.JsonCustomProtocol
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.{Failure, Success}

/*
Elegimos reducir a este archivo el manejo de ficheros que emplea Java y una programación menos funcional
 */
object FilesManagement extends JsonCustomProtocol {

  private val logger = Logger("IOFile")

  def getFile(path: String): File =
    new File(path)

  /**
   *
   * @param file   File with the Data in Json format
   * @param reader How to deserializer Json file
   * @tparam T Type of the object you are loading
   * @return Data loaded as an object
   */
  def loadJsonData[T](file: File)(implicit reader: JsonReader[T]): T = {
    val sourceFile = Source.fromFile(file)
    val jsonData = sourceFile.mkString.parseJson
    sourceFile.close()
    jsonData.convertTo[T]
  }

  def loadJsonData[T](file: File)(implicit reader: JsonReader[T], ex: ExecutionContext): Future[T] = {
    Future {
      val sourceFile = Source.fromFile(file)
      val jsonData = sourceFile.mkString.parseJson
      sourceFile.close()
      jsonData.convertTo[T]
    }
  }

  /**
   * Save data into a Json file and
   *
   * @param t      Object to save
   * @param file   File where you will save
   * @param writer How to convert it into a JSON file
   * @tparam T Object type
   * @return Object you are saving
   */
  def saveDataAsJson[T](t: T)(file: File)(implicit writer: JsonWriter[T]): T = {
    val printer = new PrintWriter(file)
    printer.write(t.toJson.prettyPrint)
    printer.close()
    t
  }

  def saveDataAsJson[T](t: Future[T])(file: File)(implicit writer: JsonWriter[T], ex: ExecutionContext): Future[T] = {
    t.onComplete {
      case Failure(_) => logger.error("The data could NOT be saved")
      case Success(value) =>
        val printer = new PrintWriter(file)
        printer.write(value.toJson.prettyPrint)
        printer.close()
    }
    t
  }

}
