package com.wacai.config.annotation

import scala.concurrent.duration._

trait T extends Configurable {
  val i: Int
  val l: Long
  val s: String
  val d: Double
  val b: Boolean
  val t: Duration
}

abstract class C extends Configurable {
  val s: String
}