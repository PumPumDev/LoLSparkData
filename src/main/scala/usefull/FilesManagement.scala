package usefull

import java.io.{File, PrintWriter}

import json.protocol.JsonCustomProtocol
import spray.json._

import scala.io.Source

/*
Elegimos reducir a este archivo el manejo de ficheros que emplea Java y una programación menos funcional
 */
object FilesManagement extends JsonCustomProtocol {


  /*
  val challengerPlayersFile: File =
    new File(config.getString("outputPath") + "challengerPlayers.json")
  val challengerSummonersFile: File =
    new File(config.getString("outputPath") + "challengerSummoners.json")
  val challengerMatchlistFile: File =
    new File(config.getString("outputPath") + "challengerMatchlist.json")

  val challengerMatchesFile: File =
    new File(config.getString("outputPath") + "challenger1000Matches.json")
  */

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

}
