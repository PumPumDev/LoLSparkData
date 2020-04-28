import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.Source

import scala.concurrent.ExecutionContextExecutor


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

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  val source: Source[Entero, NotUsed] = Source(List(Entero(1), Entero(2), Entero(3), Entero(4)))


  //print(Await.result(jsons,Duration.Inf))

  case class Entero(num: Int)
  //Await.result(source.via(flow).toMat(sink)(Keep.right).run(),Duration.Inf)
}
