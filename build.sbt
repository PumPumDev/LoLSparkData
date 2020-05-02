
// Root project
lazy val root = (project in file("."))
  .aggregate(aClient, qScala, qSpark, utils, models)
  .settings(commonSettings)

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

  version := "1.1",
  scalaVersion := "2.12.8"

)

lazy val models = project
  .settings(name := "Models",
    commonSettings)

lazy val utils = project
  .settings(
    name := "Utils",
    commonSettings,
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
    commonSettings
  ).dependsOn(utils, models)

// Refactor DTOs out of aClient project (common module)
lazy val qScala = (project in file("queries-scala"))
  .settings(
    name := "Scala Queries",
    commonSettings
  ).dependsOn(models, utils)

lazy val qSpark = (project in file("queries-spark"))
  .settings(
    name := "Spark Queries",
    commonSettings,

    // https://mvnrepository.com/artifact/org.apache.spark/spark-core
    libraryDependencies += "org.apache.spark" %% "spark-core" % "3.0.0-preview2",

    // https://mvnrepository.com/artifact/org.apache.spark/spark-sql
    libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.0.0-preview2"

  ).dependsOn(models)