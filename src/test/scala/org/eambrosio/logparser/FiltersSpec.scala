package org.eambrosio.logparser

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.testkit.TestKit
import org.scalatest.{AsyncWordSpecLike, Matchers}

class FiltersSpec
    extends TestKit(ActorSystem("GenericRecordAggregatorGraphBuilderSpec"))
    with Filters
    with AsyncWordSpecLike
    with Matchers {

  implicit val mat = ActorMaterializer(ActorMaterializerSettings(system))

  "toHostAndDatesFilter should return just one host which connect to the given host " in {
    val logTraces                  = Source(List(Connection(1L, "from1", "to"), Connection(3L, "from2", "to")))
    val zeroList: List[Connection] = List.empty

    val result = logTraces
      .via(toHostAndDatesFilter("to", 0L, 2L))
      .runWith(Sink.fold(zeroList)((acc, elem) => acc :+ elem))

    result.map {
      case List(log) => log.from shouldBe "from1"
    }
  }

  "toHostAndDatesFilter should return an empty list when no host pass the filter " in {
    val logTraces                  = Source(List(Connection(1L, "from1", "to1"), Connection(3L, "from2", "to3")))
    val zeroList: List[Connection] = List.empty

    val result = logTraces
      .via(toHostAndDatesFilter("to2", 0L, 2L))
      .runWith(Sink.fold(zeroList)((acc, elem) => acc :+ elem))

    result.map {
      case list => list shouldBe empty
    }
  }

  "toHostFilter should return those hosts which connect to the given host " in {
    val log1                       = Connection(1L, "from1", "to1")
    val log2                       = Connection(3L, "from2", "to2")
    val log3                       = Connection(3L, "from3", "to1")
    val logTraces                  = Source(List(log1, log2, log3))
    val zeroList: List[Connection] = List.empty

    val result = logTraces
      .via(toHostFilter("to1"))
      .runWith(Sink.fold(zeroList)((acc, elem) => acc :+ elem))

    result.map {
      case list => list should contain theSameElementsAs List(log1, log3)
    }
  }

  "toHostFilter should return an empty list when no hosts connect to the given host " in {
    val log1                       = Connection(1L, "from1", "to1")
    val log2                       = Connection(3L, "from2", "to2")
    val log3                       = Connection(3L, "from3", "to1")
    val logTraces                  = Source(List(log1, log2, log3))
    val zeroList: List[Connection] = List.empty

    val result = logTraces
      .via(toHostFilter("to3"))
      .runWith(Sink.fold(zeroList)((acc, elem) => acc :+ elem))

    result.map {
      case list => list shouldBe empty
    }
  }

  "fromHostFilter should return those hosts which connect from the given host " in {
    val log1                       = Connection(1L, "from1", "to1")
    val log2                       = Connection(3L, "from2", "to2")
    val log3                       = Connection(3L, "from2", "to1")
    val logTraces                  = Source(List(log1, log2, log3))
    val zeroList: List[Connection] = List.empty

    val result = logTraces
      .via(fromHostFilter("from2"))
      .runWith(Sink.fold(zeroList)((acc, elem) => acc :+ elem))

    result.map {
      case list => list should contain theSameElementsAs List(log2, log3)
    }
  }

  "fromHostFilter should return an empty list when no hosts connect from the given host " in {
    val log1                       = Connection(1L, "from1", "to1")
    val log2                       = Connection(3L, "from2", "to2")
    val log3                       = Connection(3L, "from2", "to1")
    val logTraces                  = Source(List(log1, log2, log3))
    val zeroList: List[Connection] = List.empty

    val result = logTraces
      .via(fromHostFilter("from3"))
      .runWith(Sink.fold(zeroList)((acc, elem) => acc :+ elem))

    result.map {
      case list => list shouldBe empty
    }
  }
}
