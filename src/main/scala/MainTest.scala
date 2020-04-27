import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}


object MainTest extends App {

  //val conf : SparkConf = new SparkConf().setAppName("MainTestApp").setMaster("local[2]")

  //val spark : SparkContext = new SparkContext(conf)


  /*val sparkSession: SparkSession = SparkSession.builder().appName("MainTestApp")
    .master("local[2]").getOrCreate()
  //sparkSession.sparkContext.setLogLevel("ERROR")


  import sparkSession.implicits._

  println(List(1, 2, 3, 4).toDS
    .map(_ + 1)
    .filter(i => i % 2 == 0)
    .reduce(_ + _))
*/

  import io.circe._
  import io.circe.generic.auto._
  import io.circe.parser._
  import io.circe.syntax._

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val source: Source[Entero, NotUsed] = Source(List(Entero(1), Entero(2), Entero(3), Entero(4)))
  val flow1 = Flow.fromFunction[Entero, Json](_.asJson)
  val flow = Flow.fromFunction[Json, Entero](_.as[Entero].right.get)
  val sink = Sink.collection[Entero, Seq[Entero]]
  val dto = Entero(2)
  val json: Json = dto.asJson
  val what = decode[Entero](json.noSpaces)
  //print(what)
  val dto2: Either[Error, Entero] = decode[Entero](json.noSpaces)

  case class Entero(num: Int)

  println(Await.result(source.via(flow1).via(flow).toMat(sink)(Keep.right).run(), Duration.Inf))
  Await.result(system.terminate(), Duration.Inf)
  //print(Await.result(jsons,Duration.Inf))

  //Await.result(source.via(flow).toMat(sink)(Keep.right).run(),Duration.Inf)
}
