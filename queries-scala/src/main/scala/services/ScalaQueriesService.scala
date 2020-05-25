package services

import akka.stream.Materializer
import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContext


object ScalaQueriesService {
  private val logger = Logger("Scala Queries Service")

  private val MAX_ELEMS: Integer = 100

  //TODO: Uso iterators
  //def getChallengerPlayers(outputPath: String)(implicit mat: Materializer, ex: ExecutionContext) = ???


  //def getChallengerMatches(outputPath: String)(implicit mat: Materializer, ex: ExecutionContext) = ???
}
