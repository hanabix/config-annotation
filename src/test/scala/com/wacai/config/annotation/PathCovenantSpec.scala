package com.wacai.config.annotation

import org.scalatest.{Matchers, FlatSpec}

class PathCovenantSpec extends FlatSpec with Matchers {
  "Path of class name" should "uncapitalized with under line" in {
    Macro.path(classOf[PathCovenantSpec], "i") shouldBe "path_covenant_spec.i"
  }

  "Path of class name" should "ignore neither letter or digit " in {
    Macro.path(Macro.getClass, "i") shouldBe "macro.i"
  }

  "Path of field name" should "uncapitalized with dot" in {
    Macro.path(classOf[PathCovenantSpec], "maxSize") shouldBe "path_covenant_spec.max.size"
  }
}
