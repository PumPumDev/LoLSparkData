import graph.Graph
import org.scalajs.dom
import org.scalajs.dom.raw
import table.Table

import scala.scalajs.js
import scala.scalajs.js.{Dictionary, JSON}
object VisualizationMain extends App {

    val root: raw.Element = dom.document.getElementById("root")

  val fileInput = dom.document.getElementById("dataFile").asInstanceOf[dom.html.Input]

  fileInput.onchange = _ => {
    root.innerHTML=""
    val reader = new dom.FileReader()
    reader.readAsText(fileInput.files(0))
    if (fileInput.files(0).name.contains(".tab")) {
      reader.onload = _ => {



        val data = reader.result.asInstanceOf[String].split("\n")

        val usefullData: Array[js.Tuple2[String, Dictionary[js.Array[String]]]] = data.map(singJson => {

          JSON.parse(singJson).asInstanceOf[js.Tuple2[String, js.Dictionary[js.Array[String]]]]
        })

        root.appendChild(Table(usefullData).render)

      }
    } else if (fileInput.files(0).name.contains(".graph.id")) {
      reader.onload = _ => {

        val data = reader.result.asInstanceOf[String]

        val usefullData = JSON.parse(data).asInstanceOf[js.Tuple2[String, js.Dictionary[js.Tuple2[js.Array[Int], js.Array[Double]]]]]

        root.appendChild(Graph[Int, Double](usefullData).render)
        Graph.appendPlotID(usefullData)
      }
    } else if (fileInput.files(0).name.contains(".graph.ii")){
      reader.onload = _ => {

        val data = reader.result.asInstanceOf[String]

        val usefullData = JSON.parse(data).asInstanceOf[js.Tuple2[String, js.Dictionary[js.Tuple2[js.Array[Int], js.Array[Int]]]]]

        root.appendChild(Graph[Int,Int](usefullData).render)
        Graph.appendPlotII(usefullData)
      }
    } else if(fileInput.files(0).name.contains(".graph.sd")){
      reader.onload = _ => {

        val data = reader.result.asInstanceOf[String]

        val usefullData = JSON.parse(data).asInstanceOf[js.Tuple2[String, js.Dictionary[js.Tuple2[js.Array[String], js.Array[Double]]]]]

        root.appendChild(Graph.regionGraph[Double](usefullData).render)
        Graph.appendPlotSD(usefullData)
      }
    } else if(fileInput.files(0).name.contains(".graph.si")){
      reader.onload = _ => {

        val data = reader.result.asInstanceOf[String]

        val usefullData = JSON.parse(data).asInstanceOf[js.Tuple2[String, js.Dictionary[js.Tuple2[js.Array[String], js.Array[Int]]]]]

        root.appendChild(Graph.regionGraph[Int](usefullData).render)
        Graph.appendPlotSI(usefullData)
      }
    } else if (fileInput.files(0).name.contains(".graph.dd")){
      reader.onload = _ => {

        val data = reader.result.asInstanceOf[String]

        val usefullData = JSON.parse(data).asInstanceOf[js.Tuple2[String, js.Dictionary[js.Tuple2[js.Array[Double], js.Array[Double]]]]]

        root.appendChild(Graph[Double,Double](usefullData).render)
        Graph.appendPlotDD(usefullData)
      }
    } else if (fileInput.files(0).name.contains(".graph.il")){
      reader.onload = _ => {

        val data = reader.result.asInstanceOf[String]

        val usefullData = JSON.parse(data).asInstanceOf[js.Tuple2[String, js.Dictionary[js.Tuple2[js.Array[Int], js.Array[Long]]]]]

        root.appendChild(Graph[Int,Long](usefullData).render)
        Graph.appendPlotIL(usefullData)
      }
    } else
      throw new IllegalArgumentException("El archivo pasado NO es un archivo de visualización (.tab o .graph)")
  }

}
