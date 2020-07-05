package configuration

import akka.http.scaladsl.model.headers.RawHeader
import com.typesafe.config.{Config, ConfigFactory}

// Make different configuration objects for each module?
object Configuration {
  val config: Config = ConfigFactory.load

  // Uris load
  lazy val challengerPlayerUri: String = config.getString("riot.api.uri.challenger.player")
  lazy val challengerSummonerUri: String = config.getString("riot.api.uri.challenger.summoner")
  lazy val challengerMatchlistUri: String = config.getString("riot.api.uri.challenger.matchlist")
  lazy val challengerMatchUri: String = config.getString("riot.api.uri.challenger.match")

  // Credentials load
  lazy val headers: List[RawHeader] = getApiHeaders("riot.api.token", "riot.api.keys")

  // OutputPath load
  lazy val outputPath: String = config.getString("riot.data.output.path")

  // OutputPath spark result
  lazy val dataResultPath: String = config.getString("data.visualization.output.path")

  // API statistics load
  lazy val printApiStats: Boolean = config.getBoolean("riot.data.print.statistics")

  private def getApiHeaders(header: String, keys: String): List[RawHeader] = {
    import collection.JavaConverters
    JavaConverters.asScalaBuffer(config.getStringList(keys)).toList.map(key => RawHeader(config.getString(header), key))
  }
}
