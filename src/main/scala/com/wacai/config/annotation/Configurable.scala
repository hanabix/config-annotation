package com.wacai.config.annotation

import com.typesafe.config.Config

trait Configurable {
  val config: Config
}
