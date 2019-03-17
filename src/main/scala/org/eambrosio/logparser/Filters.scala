package org.eambrosio.logparser

import java.text.SimpleDateFormat
import java.util.Date

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.typesafe.scalalogging.LazyLogging

trait Filters extends LazyLogging {
  val dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")

  def fromHostFilter(host: String): Flow[Connection, Connection, NotUsed] =
    Flow[Connection]
      .filter(_.from == host)

  def toHostFilter(host: String): Flow[Connection, Connection, NotUsed] =
    Flow[Connection]
      .filter(_.to == host)

  def toHostAndDatesFilter(
      host: String,
      startDate: Long,
      endDate: Long): Flow[Connection, Connection, NotUsed] = {
    Flow[Connection]
      .filter { log =>
        log.time >= startDate && log.time <= endDate && log.to == host
      }
      .map { log =>
        logger.info(s"HOST CONNECTED TO $host (from ${dateFormatter
          .format(new Date(startDate))} to ${dateFormatter.format(new Date(endDate))}):")
        logger.info(s"******FROM HOST: ${log.from} **************")
        log
      }
  }
}
