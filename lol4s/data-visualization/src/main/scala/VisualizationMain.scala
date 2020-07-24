import graph.Graph
import org.scalajs.dom
import org.scalajs.dom.raw
import table.Table
import types._

import scala.scalajs.js
import scala.scalajs.js.JSON
object VisualizationMain extends App {

    private val root: raw.Element = dom.document.getElementById("root")

    private val fileInput = dom.document.getElementById("dataFile").asInstanceOf[dom.html.Input]

  fileInput.onchange = _ => {
    root.innerHTML=""
    val reader = new dom.FileReader()
    reader.readAsText(fileInput.files(0))
    reader.onload = _ =>{
      val data = reader.result.asInstanceOf[String]
      val fileName: String = fileInput.files(0).name
      try{
        val usefulData: js.Dynamic = JSON.parse(data)
        fileName match {
          case s if s.endsWith(types.Table.extension) => graphWrapper(usefulData,root,types.Table)
          case s if s.endsWith(Gid.extension) => graphWrapper(usefulData,root,Gid)
          case s if s.endsWith(Gii.extension) => graphWrapper(usefulData,root,Gii)
          case s if s.endsWith(Gsd.extension) => graphWrapper(usefulData,root,Gsd)
          case s if s.endsWith(Gsi.extension) => graphWrapper(usefulData,root,Gsi)
          case s if s.endsWith(Gdd.extension) => graphWrapper(usefulData,root,Gdd)
          case _ => throw new IllegalArgumentException("El archivo pasado NO es un archivo de visualización (.tab o .graph)")
        }
      }
      catch {
        case e: Throwable => throw new IllegalArgumentException(s"No se ha podido parsear correctamente el archivo JSON\n$e")
      }
    }
  }


  private def graphWrapper(data: js.Dynamic, root: raw.Element, graphTypes: GraphTypes): Unit =
    try {
      graphTypes match {
        case types.Table =>
          val typedData = data.asInstanceOf[js.Tuple2[String, js.Dictionary[js.Array[String]]]]
          root.appendChild(Table(typedData).render)
        case Gii =>
          val typedData = data.asInstanceOf[js.Tuple2[String, js.Dictionary[js.Tuple2[js.Array[Int], js.Array[Int]]]]]
          root.appendChild(Graph[Int, Int](typedData).render)
          Graph.appendPlotII(typedData)
        case Gdd =>
          val typedData = data.asInstanceOf[js.Tuple2[String, js.Dictionary[js.Tuple2[js.Array[Double], js.Array[Double]]]]]
          root.appendChild(Graph[Double,Double](typedData).render)
          Graph.appendPlotDD(typedData)
        case Gid =>
          val typedData = data.asInstanceOf[js.Tuple2[String, js.Dictionary[js.Tuple2[js.Array[Int], js.Array[Double]]]]]
          root.appendChild(Graph[Int, Double](typedData).render)
          Graph.appendPlotID(typedData)
        case Gsi =>
          val typedData = data.asInstanceOf[js.Tuple2[String, js.Dictionary[js.Tuple2[js.Array[String], js.Array[Int]]]]]
          root.appendChild(Graph.regionGraph[Int](typedData).render)
          Graph.appendPlotSI(typedData)
        case Gsd =>
          val typedData = data.asInstanceOf[js.Tuple2[String, js.Dictionary[js.Tuple2[js.Array[String], js.Array[Double]]]]]
          root.appendChild(Graph.regionGraph[Double](typedData).render)
          Graph.appendPlotSD(typedData)
      }
    } catch {
      case e: ClassCastException => new ClassCastException(s"Ha habido un problema al establecer los tipos de los datos\n$e")
    }
}
