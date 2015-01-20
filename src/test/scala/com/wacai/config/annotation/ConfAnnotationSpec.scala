package com.wacai.config.annotation

import com.typesafe.config.ConfigFactory
import org.scalatest._

class ConfAnnotationSpec extends FlatSpec with Matchers {
  "@conf annotated class" should "get value" in {
    import scala.concurrent.duration._

    val c = new C
    c.i shouldBe 10
    c.l shouldBe 4L * 1024 * 1024 * 1024
    c.s shouldBe "abc"
    c.d shouldBe 3.14
    c.b shouldBe false
    c.t shouldBe 5.seconds
  }

  "@conf annotated module" should "get value from parsed config" in {
    O.i shouldBe 10
  }

  "@conf annotated trait" should "get value from parsed config" in {
    new T {}.proxy shouldBe 10
  }

  "@conf annotated class with companion" should "get value" in {
    new CO {}.i shouldBe 10
  }
}

@conf[ConfDef1] class C

@conf[ConfDef2] object O extends Configurable {
  def config = ConfigFactory.parseString("conf_def2.i = 10")
}

@conf[ConfDef2] trait T extends Configurable {
  def config = ConfigFactory.parseString("conf_def2.i = 10")

  def proxy = i
}

object CO

@conf[ConfDef1] class CO

trait ConfDef1 {

  import scala.concurrent.duration.Duration

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
