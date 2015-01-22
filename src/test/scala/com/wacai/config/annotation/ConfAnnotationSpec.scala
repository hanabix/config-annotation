package com.wacai.config.annotation

import com.typesafe.config.ConfigFactory
import org.scalatest._
import scala.concurrent.duration._


class ConfAnnotationSpec extends FlatSpec with Matchers {
  "@conf annotated trait" should "get value" in {
    val conf = new kafka {}

    conf.server.host shouldBe "wacai.com"
    conf.server.port shouldBe 12306
    conf.socket.timeout shouldBe 3.seconds
    conf.socket.buffer shouldBe 1024 * 1024L
    conf.client shouldBe "wacai"
  }

}

@conf trait kafka extends Configurable {

  val server = new {
    val host = "localhost"
    val port = 9092
  }

  val socket = new {
    val timeout = 5 seconds
    val buffer = 1024 * 64L
  }

  val client = "id"

  def config = ConfigFactory.parseString(
    """
      |kafka {
      |  server {
      |    host: wacai.com
      |    port: 12306
      |  }
      |
      |  socket {
      |    timeout = 3s
      |    buffer  = 1M
      |  }
      |
      |  client: wacai
      |}
    """.stripMargin)
}