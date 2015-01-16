package com.wacai.config

import reflect.macros.blackbox

/**
 * Context has been deprecated in Scala 2.11, blackbox.Context is used instead
 */
object CrossVersionDefs {
  type CrossVersionContext = blackbox.Context
}
