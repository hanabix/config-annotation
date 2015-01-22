package com.wacai.config.annotation

import annotation.StaticAnnotation

class conf extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro Macro.impl
}
