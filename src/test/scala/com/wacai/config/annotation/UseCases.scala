package com.wacai.config.annotation

import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

class Val {
  @conf val i = 0
  @conf val l = 0L
  @conf val s = ""
  @conf val d = 0.0
  @conf val b = true
  @conf val t = 1 second
}

class Var {
  @conf var i: Int      = _
  @conf var l: Long     = _
  @conf var d: Double   = _
  @conf var b: Boolean  = _
  @conf var s: String   = _
  @conf var t: Duration = _
}

class CustomizedConfig extends Configurable {
  lazy val config = ConfigFactory.parseString("customized_config.s = abc")

  @conf val s = ""
}