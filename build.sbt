
name := "akka-quickstart-scala"

version := "1.0"

scalaVersion := "2.12.6"

lazy val akkaVersion = "2.5.21"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.5" % "test",
  "com.typesafe.akka" %% "akka-stream-kafka" % "1.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.log4s" %% "log4s" % "1.6.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3"

)
