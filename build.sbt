

//Common settings
lazy val commonSettings = Seq(
  target := {
    baseDirectory.value / "target"
  },

  //Load properties
  libraryDependencies += "com.typesafe" % "config" % "1.4.0",

  //Manage Log
  libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3",

  version := "1.2"
)

lazy val models = project
  .settings(name := "Models",
    commonSettings)
  .cross
lazy val models_2_11 = models("2.11.12")
lazy val models_2_12 = models("2.12.8")

lazy val utils = project
  .settings(
    name := "Utils",
    commonSettings,
    scalaVersion := "2.12.8",
    // Client Akka HTTP dependencies
    libraryDependencies += "com.typesafe.akka" %% "akka-http-core" % "10.1.11",

    libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.11",

    // Akka Stream dependency
    libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.4",

    // Circe dependency (to Json manage)
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % "0.12.3")
  )

lazy val aClient = (project in file("api-client"))
  .settings(
    name := "Client API",
    scalaVersion := "2.12.8",
    assemblyJarName in assembly := "Client_API.jar",
    commonSettings
  ).dependsOn(utils, models_2_12)

// Refactor DTOs out of aClient project (common module)
lazy val qScala = (project in file("queries-scala"))
  .settings(
    name := "Scala Queries",
    scalaVersion := "2.12.8",
    commonSettings
  ).dependsOn(models_2_12, utils)

lazy val qSpark = (project in file("queries-spark"))
  .settings(
    name := "Spark Queries",
    scalaVersion := "2.11.12",
    assemblyJarName in assembly := "Queries_Spark.jar",
    assemblyMergeStrategy in assembly := {
      case PathList("org", "apache", xs@_*) => MergeStrategy.last
      case PathList("javax", "inject", xs@_*) => MergeStrategy.last
      case PathList("org", "aopalliance", xs@_*) => MergeStrategy.last
      case PathList("javax", "inject", xs@_*) => MergeStrategy.last
      case PathList("org", "slf4j", xs@_*) => MergeStrategy.last
      case PathList(ps@_*) if ps.last endsWith "git.properties" => MergeStrategy.last
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    commonSettings,
    // https://mvnrepository.com/artifact/org.apache.spark/spark-core
    libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.5",

    // https://mvnrepository.com/artifact/org.apache.spark/spark-sql
    libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.5"


  )
  .aggregate(models_2_11)
  .dependsOn(models_2_11)


// Root project
lazy val TfgInf = (project in file("."))
  .aggregate(utils, models_2_12, aClient, qScala, qSpark)
  .settings(commonSettings)