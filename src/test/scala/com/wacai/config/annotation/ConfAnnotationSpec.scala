package com.wacai.config.annotation

import com.typesafe.config.{ConfigFactory, Config}
import org.scalatest._
import scala.concurrent.duration._

class ConfAnnotationSpec extends FlatSpec with Matchers {
  "@conf annotated class" should "get value" in {

    @conf[ConfDef1] class C

    val c = new C
    c.i shouldBe 10
    c.l shouldBe 4L * 1024 * 1024 * 1024
    c.s shouldBe "abc"
    c.d shouldBe 3.14
    c.b shouldBe false
    c.t shouldBe 5.seconds
  }

  "@conf annotated customized config" should "get value from parsed config" in {

    @conf[ConfDef2] object O extends Configurable {
      def config = ConfigFactory.parseString("conf_def2.i = 10")
    }

    O.i shouldBe 10
  }
}


trait ConfDef1 {
  val i: Int
  val l: Long
  val s: String
  val d: Double
  val b: Boolean
  val t: Duration
}

trait ConfDef2 {
  val i: Int
}
