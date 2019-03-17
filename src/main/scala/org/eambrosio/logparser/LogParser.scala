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

object LogParser extends App with LazyLogging {
  private val config        = ConfigFactory.load()
  val initDate              = config.getLong("init_date")
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
  val extractLog: Flow[ConsumerRecord[String, String], Log, NotUsed] =
    Flow[ConsumerRecord[String, String]]
      .map { record =>
        val logTrace: Array[String] = record.value().split(" ")
        Log(logTrace(0).toLong, logTrace(1), logTrace(2))
      }

  val zeroLogs: List[Log] = List.empty

  val aggTo: Flow[Log, List[Log], NotUsed] = Flow[Log]
    .groupBy(Int.MaxValue, _.to)
    .scan(zeroLogs)((list, log) => list :+ log)
    .mergeSubstreams
    .map { list =>
      //      println(list)
      list

    }

  def filterFromHost(host: String): Flow[Log, Log, NotUsed] =
    Flow[Log]
      .filter(_.from == host)

  def filterToHost(host: String): Flow[Log, Log, NotUsed] =
    Flow[Log]
      .filter(_.to == host)

  val aggFrom: Flow[Log, List[Log], NotUsed] = Flow[Log]
    .groupBy(Int.MaxValue, _.from)
    .scan(zeroLogs)((list, log) => list :+ log)
    .mergeSubstreams
    .map { list =>
      println(list)
      list

    }

  val filter: Flow[Log, Log, NotUsed] = Flow[Log]
    .filter { log =>
      log.time >= initDate && log.time <= endDate && log.to == ConnectedToHostname
    }
    .map { log =>
      println(s"****** FILTERED LOG: $log **************")
      log
    }

  val print: Flow[Seq[List[Log]], List[Log], NotUsed] = Flow[Seq[List[Log]]]
    .map { list =>
      println(list)
      list.flatten.toList
    }

  def print(host: String): Flow[Seq[Log], Seq[Log], NotUsed] =
    Flow[Seq[Log]]
      .map { log =>
        println(s"*** CONNECTED $host: $log ****************")
        log
      }

  source
    .via(extractLog)
    .alsoTo(filter.to(Sink.ignore))
    .alsoTo(
      filterToHost(ConnectedToHostname)
        .groupedWithin(Int.MaxValue, duration)
        .via(print("TO"))
        .to(Sink.ignore))
    .alsoTo(
      filterFromHost(ConnectedFromHostname)
        .groupedWithin(Int.MaxValue, duration)
        .via(print("FROM"))
        .to(Sink.ignore))
    .runWith(Sink.ignore)

}

case class Log(time: Long, from: String, to: String)

case class ConnectionsFrom(host: String, connections: List[(Long, String)])

case class ConnectionsTo(host: String, connections: List[(Long, String)])
