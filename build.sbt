
name := "logparser"

version := "1.0"

scalaVersion := "2.12.6"

val akkaVersion = "2.5.21"
val scalaTestVersion = "3.0.5"
val akkaStreamKafkaVersion = "1.0"
val scalaLoggingVersion = "3.9.2"
val log4sVersion = "1.6.1"
val logbackVersion = "1.2.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % akkaStreamKafkaVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
  "org.log4s" %% "log4s" % log4sVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "org.scalatest" %% "scalatest" % scalaTestVersion % "test"

)
