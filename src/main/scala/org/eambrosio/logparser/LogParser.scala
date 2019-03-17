package org.eambrosio.logparser

import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.kafka.Subscriptions
import akka.kafka.scaladsl.Consumer
import akka.stream._
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.{kafka, NotUsed}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.duration.FiniteDuration

object LogParser extends App with LazyLogging with Filters {
  private val config        = ConfigFactory.load()
  val startDate             = config.getLong("init_date")
  val endDate               = config.getLong("end_date")
  val ConnectedFromHostname = config.getString("from_hostname")
  val ConnectedToHostname   = config.getString("to_hostname")
  val duration: FiniteDuration =
    FiniteDuration(config.getDuration("duration").toNanos, TimeUnit.NANOSECONDS)

  implicit val system       = ActorSystem("LogParser")
  implicit val materializer = ActorMaterializer()

  val settings = kafka
    .ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
    .withGroupId(UUID.randomUUID().toString)

  val source: Source[ConsumerRecord[String, String], Consumer.Control] =
    Consumer.plainSource(settings, Subscriptions.topics("logs"))
  val extractLog: Flow[ConsumerRecord[String, String], Connection, NotUsed] =
    Flow[ConsumerRecord[String, String]]
      .map { record =>
        val logTrace: Array[String] = record.value().split(" ")
        Connection(logTrace(0).toLong, logTrace(1), logTrace(2))
      }

  def print(hostType: String, hostName: String): Flow[Seq[String], Seq[String], NotUsed] =
    Flow[Seq[String]]
      .map { log =>
        logger.info(
          s"\nHOSTS CONNECTED $hostType HOST '$hostName' DURING THE LAST ${duration.toSeconds} SECOND(S):\n${log
            .mkString("\n")}")
        log
      }

  source
    .via(extractLog)
    .alsoTo(toHostAndDatesFilter(ConnectedToHostname, startDate, endDate).to(Sink.ignore))
    .alsoTo(
      toHostFilter(ConnectedToHostname)
        .groupedWithin(Int.MaxValue, duration)
        .map(logs => logs.map(_.from))
        .via(print("TO", ConnectedToHostname))
        .to(Sink.ignore))
    .alsoTo(
      fromHostFilter(ConnectedFromHostname)
        .groupedWithin(Int.MaxValue, duration)
        .map(logs => logs.map(_.to))
        .via(print("FROM", ConnectedFromHostname))
        .to(Sink.ignore))
    .runWith(Sink.ignore)

}

case class Connection(time: Long, from: String, to: String)
