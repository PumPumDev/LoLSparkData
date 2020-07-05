package treatment


import java.io.{BufferedWriter, File, FileWriter}

import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}
import dto.RegionDTO
import io.circe.Encoder
import org.apache.spark.sql.{Column, DataFrame}
import com.typesafe.scalalogging.Logger

import scala.reflect.io.Directory
import configuration.Configuration.{dataResultPath, AWSLaunch}


object DataTreatment {
  private val log = Logger("Data Treatment")

  def tablesPerRegion(title: String, df: DataFrame): Unit = {
    import df.sparkSession.implicits._
    val info: Map[String, Array[String]] = RegionDTO.getAllRegions.map(reg => {
      val consult: Array[String] = df.filter($"region"===reg.toString).limit(20)
        .collect().map(f = row => row.toString())

      val headerAsString = df.columns.foldLeft[String]("[")((res, ele) => if (res.equals("[")) res++ele else res++","++ele)++"]"

      reg.toString -> consult.+:(headerAsString)
    }).toMap

     toVisualizationTab(title->info, AWSLaunch)
  }


  //TODO: Test AWS write the visualization files
  private def toVisualizationTab(consultResult: (String, Map[String, Array[String]]), aws: Boolean): Unit = {
    import io.circe.syntax._

    val file = new File(s"$dataResultPath/tables/${consultResult._1.replace(" ","_").toLowerCase}.tab")

    if (!file.exists() && file.getParentFile != null)
      file.getParentFile.mkdirs()
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(consultResult.asJson.noSpaces+"\n")
    bw.close()

    //todo: bucket name generic
    if (aws){
      val s3: AmazonS3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build()
      s3.putObject(s"league-data/${file.getParentFile.getPath}",file.getName, file)
    }

    log.info(s"Se generado el archivo ${file.getName} correctamente")
   }

  // df it s a Dataframe with 2 columns, first is the X in the Graph and second the Y
  def graphPerRegion[A,B](title: String, df: DataFrame, colX: Column, colY: Column, graphsTypes: GraphsTypes)
                         (implicit encA: Encoder[A], encB: Encoder[B]): Unit = {
    import df.sparkSession.implicits._

    val info: Map[String, (Seq[A], Seq[B])] = RegionDTO.getAllRegions.map(reg => {
      val xData = df.filter($"region"===reg.toString).select(colX).collect().map(_.get(0).asInstanceOf[A]).toSeq
      val yData = df.filter($"region"===reg.toString).select(colY).collect().map(_.get(0).asInstanceOf[B]).toSeq

      reg.toString -> (xData,yData)
    }).toMap

    toVisualizationGraph[A,B](title -> info, graphsTypes, AWSLaunch)
  }

  private def toVisualizationGraph[A, B](dataResult: (String, Map[String, (Seq[A], Seq[B])]), graphsTypes: GraphsTypes, aws: Boolean)
                                        (implicit encA: Encoder[A], encB: Encoder[B]): Unit = {
    import io.circe.syntax._

    val file = new File(s"$dataResultPath/graphs/${dataResult._1.replace(" ", "_").toLowerCase}.graph${graphsTypes.extension}")

    if (!file.exists() && file.getParentFile!=null)
      file.getParentFile.mkdirs()
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(dataResult.asJson.noSpaces+"\n")
    bw.close()

    if (aws) {
      val s3: AmazonS3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_1).build()
      s3.putObject(s"league-data/${file.getParentFile.getPath}",file.getName, file)
    }

    log.info(s"Se generado el archivo ${file.getName} correctamente")
  }

  def cleanVisualData(): Unit = {
    new Directory(new File(s"$dataResultPath")).deleteRecursively()
  }


}
