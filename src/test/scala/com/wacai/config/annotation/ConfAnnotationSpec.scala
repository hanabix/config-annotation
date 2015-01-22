package com.wacai.config.annotation

import com.typesafe.config.ConfigFactory
import org.scalatest._

class ConfAnnotationSpec extends FlatSpec with Matchers {
  "@conf annotated trait" should "get value" in {
    val conf = new kafka {}

    conf.server.host shouldBe "wacai.com"
    conf.server.port shouldBe 12306
    conf.client shouldBe "wacai"
  }

}

@conf trait kafka extends Configurable {
  val server = new {
    val host = "localhost"
    val port = 9092
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
      |  client: wacai
      |}
    """.stripMargin)
}