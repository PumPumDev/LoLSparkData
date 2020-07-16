package graph

import org.scalajs.dom.html.Div

import scalatags.JsDom
import scalatags.JsDom.all._

import scala.scalajs.js

import plotly._, element._, layout._, Plotly._

object Graph {
  def apply[A,B](data: js.Tuple2[String, js.Dictionary[js.Tuple2[js.Array[A], js.Array[B]]]]): JsDom.TypedTag[Div] =
    div(
      h2(
        strong(
          data._1
        )
      ),
      data._2.iterator.toList.map{case (reg, _) =>
        div(
          h3(
            "Region: ",
            strong(reg)
          ),
          div(id:=s"plotDiv_$reg")
        )
      }
    )

  def regionGraph[B](data: js.Tuple2[String, js.Dictionary[js.Tuple2[js.Array[String], js.Array[B]]]]):JsDom.TypedTag[Div] = {
    div(
      h2(
        strong(data._1)
      ),
      div(id:="plotDiv")
    )
  }

  def appendPlotID(data: js.Tuple2[String, js.Dictionary[js.Tuple2[js.Array[Int],js.Array[Double]]]]): Unit = {
    data._2.iterator.toList.foreach { case (reg, tup) =>
      val trace1 = Scatter(
        tup._1.toSeq, tup._2.toSeq,
        mode = ScatterMode(ScatterMode.Markers)
        )
        Plotly.plot(s"plotDiv_$reg", Seq(trace1))
    }
  }

  def appendPlotDD(data: js.Tuple2[String, js.Dictionary[js.Tuple2[js.Array[Double],js.Array[Double]]]]): Unit = {
    data._2.iterator.toList.foreach { case (reg, tup) =>
      val trace1 = Scatter(
        tup._1.toSeq, tup._2.toSeq,
        mode = ScatterMode(ScatterMode.Markers)
      )
      Plotly.plot(s"plotDiv_$reg", Seq(trace1))
    }
  }

  def appendPlotII(data: js.Tuple2[String, js.Dictionary[js.Tuple2[js.Array[Int],js.Array[Int]]]]): Unit = {
    data._2.iterator.toList.foreach { case (reg, tup) =>
      val trace1 = Scatter(
        tup._1.toSeq, tup._2.toSeq,
        mode = ScatterMode(ScatterMode.Markers)
      )
      Plotly.plot(s"plotDiv_$reg", Seq(trace1))//, Layout(title = "Position Vs WinRate", xaxis = Axis(title = "Position"), yaxis = Axis(title = "Win Rate")))
    }
  }

  def appendPlotIL(data: js.Tuple2[String, js.Dictionary[js.Tuple2[js.Array[Int],js.Array[Long]]]]): Unit = {
    data._2.iterator.toList.foreach { case (reg, tup) =>
      val trace1 = Scatter(
        tup._1.toSeq, tup._2.toSeq,
        mode = ScatterMode(ScatterMode.Markers)
      )
      Plotly.plot(s"plotDiv_$reg", Seq(trace1))
    }
  }

  def appendPlotSI(data: js.Tuple2[String, js.Dictionary[js.Tuple2[js.Array[String],js.Array[Int]]]]): Unit = {
    val seqs: (Seq[String], Seq[Int]) = data._2.iterator.foldLeft[(Seq[String], Seq[Int])]((Seq(),Seq())){case ((seq1: Seq[String],seq2: Seq[Int]), (reg, tup)) =>
      (seq1:+reg, seq2:+tup._2.head)
    }
    Plotly.plot(s"plotDiv", Seq(Bar(seqs._1, seqs._2)))
  }

  def appendPlotSD(data: js.Tuple2[String, js.Dictionary[js.Tuple2[js.Array[String],js.Array[Double]]]]): Unit = {
    val seqs: (Seq[String], Seq[Double]) = data._2.iterator.foldLeft[(Seq[String], Seq[Double])]((Seq(),Seq())){case ((seq1: Seq[String],seq2: Seq[Double]), (reg, tup)) =>
      (seq1:+reg, seq2:+tup._2.head)
    }
    Plotly.plot(s"plotDiv", Seq(Bar(seqs._1, seqs._2)))
  }

}


