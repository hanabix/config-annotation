package com.wacai.config.annotation

import org.scalatest._
import scala.concurrent.duration._

class ConfAnnotationSpec extends FlatSpec with Matchers {
  "@conf annotated val" should "get value" in {
    val v = new Val()
    v.i shouldBe 10
    v.l shouldBe 4L * 1024 * 1024 * 1024
    v.s shouldBe "abc"
    v.d shouldBe 3.14
    v.b shouldBe false
    v.t shouldBe 5.seconds
  }

  "@conf annotated var" should "get value" in {
    val v = new Var()
    v.i shouldBe 10
    v.l shouldBe 4L * 1024 * 1024 * 1024
    v.s shouldBe "abc"
    v.d shouldBe 3.14
    v.b shouldBe false
    v.t shouldBe 5.seconds
  }

  "@conf annotated customized config" should "get value from parsed config" in {
    new CustomizedConfig().s shouldBe "abc"
  }
}
