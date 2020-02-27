name := "TfgInf"

version := "1.0"

scalaVersion := "2.12.8"

//Dependencies
// https://mvnrepository.com/artifact/org.apache.spark/spark-core
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.5"

// https://mvnrepository.com/artifact/org.apache.spark/spark-sql
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.5"

// Client Akka HTTP
libraryDependencies += "com.typesafe.akka" %% "akka-http-core" % "10.1.11"

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.11"

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.3"

//Managing JSON
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.11"

//Actor system for the concurrency
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.6.3"

//Load properties
libraryDependencies += "com.typesafe" % "config" % "1.4.0"

