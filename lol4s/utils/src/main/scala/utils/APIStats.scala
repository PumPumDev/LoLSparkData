package utils

import akka.http.scaladsl.model.HttpResponse
import com.typesafe.scalalogging.Logger

import scala.util.{Success, Try}

class APIStats(private val name: String) {

  private val logger: Logger = Logger("API Stats")
  private var successAcc: Long = 0
  private var error404Acc: Long = 0
  private var error429Acc: Long = 0
  private var errorUnknown: Long = 0

  def apply(input: (Try[HttpResponse], _)): APIStats = {
    input match {
      case (Success(value: HttpResponse), _) if value.status.isSuccess() => successAcc += 1
      case (Success(value: HttpResponse), _) if value.status.intValue() == 404 => error404Acc += 1
      case (Success(value: HttpResponse), _) if value.status.intValue() == 429 => error429Acc += 1
      case _ => errorUnknown += 1
    }
    this
  }

  def printStatisticsResult(): Unit = {
    logger.info(s"\n$name"
      + s"\nThe number of success petitions was: $successAcc\n"
      + s"The number of 429 (Too Many Request) error petitions was: $error429Acc\n"
      + s"The number of 404 (Not Found Data) error petitions was: $error404Acc\n"
      + s"The number of unknown error petitions was: $errorUnknown\n")
  }

}

object APIStats {
  def apply(name: String): APIStats = new APIStats(name)
}
