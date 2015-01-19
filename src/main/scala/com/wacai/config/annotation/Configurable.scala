package com.wacai.config.annotation

import com.typesafe.config.Config

trait Configurable {
  def config: Config
}
