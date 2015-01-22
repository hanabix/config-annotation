package com.wacai.config.annotation

import org.scalatest.{Matchers, FlatSpec}
import scala.concurrent.duration._

class UnitGenSpec extends FlatSpec with Matchers {
  "duration" should "get right unit" in {
    Macro.time(1 nanosecond) shouldBe "1ns"
    Macro.time(1 microsecond) shouldBe "1us"
    Macro.time(1 millisecond) shouldBe "1ms"
    Macro.time(1 second) shouldBe "1s"
    Macro.time(1 minute) shouldBe "1m"
    Macro.time(1 hour) shouldBe "1h"
    Macro.time(1 day) shouldBe "1d"
  }

  "bytes" should "get right unit" in {
    Macro.bytes(1023L) shouldBe "1023B"
    Macro.bytes(1025L) shouldBe "1025B"
    Macro.bytes(1024L) shouldBe "1K"
    Macro.bytes(1024L * 1024) shouldBe "1M"
    Macro.bytes(1024L * 1024 * 1024) shouldBe "1G"
  }
}
