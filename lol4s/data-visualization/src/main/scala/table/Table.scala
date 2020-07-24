package table


import org.scalajs.dom.html.{Div, Table}
import scalatags.JsDom
import scalatags.JsDom.all._

import scala.scalajs.js
import scala.scalajs.js.Dictionary


object Table {

  def apply(data: js.Tuple2[String, Dictionary[js.Array[String]]]): JsDom.TypedTag[Div] =
      div(
        h2(
          strong(
            data._1
          )
        ),
      data._2.iterator.toList.map{case (st, inf) => div(
        h3(
          "Región: ",
          strong(
            st
          )
        ),
        createDataTable(inf.map(row => row.substring(1,row.length-1))),
        br
      )}
    )

  private def createDataTable(data: js.Array[String]): JsDom.TypedTag[Table] =
    table(`class`:="blueTable",
      thead(
        tr(
          data.head.split(",").map(th(_))
        )
      ),
      tbody(id:="tableBody",
        data.drop(1).map(row => tr(
          row.split(",").map(att => td(att))
        )).toList
      )
    )
}
