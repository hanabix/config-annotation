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
    conf.debug shouldBe true
    conf.delays shouldBe 2.seconds
  }

  it should "save and load strings with special characters" in {
    val conf = new specialchars {}
    conf.url shouldBe "http://localhost:8080/"
    conf.urls shouldBe List("http://localhost:8080/")
    conf.all shouldBe List("$", "\"", "{", "}", "[", "]", ":", "=", ",", "+", "#", "`", "^", "?", "!", "@", "*", "&", "\\\\")
    conf.empty shouldBe ""
  }

  it should "get substitution value" in {
    (new concrete {} value) shouldBe 128
  }

  it should "get list value" in {
    val conf = new list {}
    conf.i shouldBe List(1, 2)
    conf.b shouldBe List(true, false)
    conf.d shouldBe List(1.1, 2.2)
    conf.l shouldBe List(512L, 1024 * 3L)
    conf.t shouldBe List(1 second, 2 minutes)
    conf.s shouldBe List("a", "b")
    conf.e shouldBe List("", "a", "", "c", "")
  }

  it should "know how to save and load Map[String,String]" in {
    val conf = new maps{}
    conf.ab shouldBe Map("a" -> "b")
    conf.abcd shouldBe Map[String,String]("a" -> "b", "c" -> "d")
  }

  it should "save and load strings with special keys use scala back ticks literal identifier" in {
    val conf = new backTicksKeys {}
    conf.`key-with-mid-line` shouldBe "dash"
    conf.`netty.tcp`.dotkey shouldBe "dot value"
    conf.`netty.port` shouldBe "port"
    conf.`netty.tcp`.level3.`level4.key` shouldBe "level4value"
    conf.`netty.tcp`.`level3.key` shouldBe "level3value"
  }
}

@conf trait kafka extends Configurable {

  val server = new {
    val host = "localhost"
    val port = 9092
  }

  val socket = new {
    val timeout = 5 seconds
    val buffer  = 1024 * 64L
  }

  val client = "id"

  val debug = false

  val delays = 2 seconds

  def config = ConfigFactory.parseString(
    """|
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
      |
      |  debug:yes
      |
      |  delays:2s
      |}
    """.stripMargin)
}

@conf trait common {
  val sub = 128
}

@conf trait concrete extends common {
  val value = sub
}

@conf trait specialchars {
  val url = "http://localhost:8080/"
  val urls = List("http://localhost:8080/")
  val all = List("$", "\"", "{", "}", "[", "]", ":", "=", ",", "+", "#", "`", "^", "?", "!", "@", "*", "&", "\\\\")
  val empty = ""
}

@conf trait list {
  val i = List(1, 2)
  val b = List(true, false)
  val d = List(1.1, 2.2)
  val l = List(512L, 1024 * 3L)
  val t = List(1 second, 2 minutes)
  val s = List("a", "b")
  val e = List("", "a", "", "c", "")
}

@conf trait maps {
  val ab = Map[String,String]("a" -> "b")
  val abcd = Map[String,String]("a" -> "b", "c" -> "d")
}

@conf trait backTicksKeys {
  val `key-with-mid-line` = "dash"
  val `netty.tcp` = new {
    val dotkey = "dot value"
    val `level3.key` = "level3value"
    val level3 = new {
      val `level4.key` = "level4value"
    }
  }
  val `netty.port` = "port"
}