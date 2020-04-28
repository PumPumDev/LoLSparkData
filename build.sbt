name := "TfgInf"

version := "1.1"

scalaVersion := "2.12.8"

//Dependencies
// https://mvnrepository.com/artifact/org.apache.spark/spark-core
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.5"

// https://mvnrepository.com/artifact/org.apache.spark/spark-sql
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.5"

// Client Akka HTTP
libraryDependencies += "com.typesafe.akka" %% "akka-http-core" % "10.1.11"

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.11"

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.4"

//Actor system for the concurrency
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.3"

//Load properties
libraryDependencies += "com.typesafe" % "config" % "1.4.0"

//Manage Log
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

//Circe (Json manage)
val circeVersion = "0.12.3"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)


