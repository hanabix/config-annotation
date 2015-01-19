package com.wacai.config.annotation

import annotation.StaticAnnotation

class conf[T] extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro Macro.impl
}
