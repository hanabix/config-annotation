package com.wacai.config.annotation

import org.scalatest.{Matchers, FlatSpec}

class PathCovenantSpec extends FlatSpec with Matchers {
  implicit val s2l = (_:String).toList

  "Path of class name" should "uncapitalized with under line" in {
    Macro.path("PathCovenantSpec", "i") shouldBe "path_covenant_spec.i"
  }

  "Path of class name" should "ignore neither letter or digit " in {
    Macro.path("Macro$", "i") shouldBe "macro.i"
  }

  "Path of field name" should "uncapitalized with dot" in {
    Macro.path("PathCovenantSpec", "maxSize") shouldBe "path_covenant_spec.max.size"
  }
}
