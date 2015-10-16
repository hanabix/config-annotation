## Config annotation

[![Build Status](https://travis-ci.org/wacai/config-annotation.png?branch=master)](https://travis-ci.org/wacai/config-annotation)
[![Codacy Badge](https://www.codacy.com/project/badge/b9158949586c439cb05e21333f52798b)](https://www.codacy.com/public/zhonglunfu/config-annotation)

A refactor-friendly configuration lib would help scala programmers to maintain [config][conf] files without any pain,
by using scala [macro annotation][mcr].

## Usage

Create a config-style trait as configuration definition, eg:

```scala
import com.wacai.config.annotation._
import scala.concurrent.duration._

@conf trait kafka {
  val server = new {
    val host = "wacai.com"
    val port = 12306
  }

  val socket = new {
    val timeout = 3 seconds
    val buffer = 1024 * 64L
  }

  val client = "wacai"
}
```

Use config by extending it,

```scala
class Consumer extends kafka {
  val client = new SimpleConsumer(
    server.host,
    server.port,
    socket.timeout,
    socket.buffer,
    client
  )

  ...
}
```

Compile, `@conf` will let scala compiler to insert codes to `kafka.scala`:

```scala
trait kafka {
  val server = new {
    val host = config.getString("kafka.server.host")
    val port = config.getInt("kafka.server.port")
  }
  val socket = new {
    val timeout = Duration(config.getDuration("kafka.socket.timeout", SECONDS))
    val buffer = config.getBytes("kafka.socket.buffer")
  }
  val client = config.getString("kafka.client")

  ...
}
```

After that, a config file named `kafka.conf` was generated at `src/main/resources` as blow:

```
kafka {
  server {
    host = wacai.com
    port = 12306
  }

  socket {
    timeout = 3s
    buffer = 64K
  }

  client = wacai
}

```

Last but not least, a `application.conf` need to be created to include `kafka.conf` like:

```
include "kafka.conf"
```


## Installation

> Caution: only support scala 2.11.0+

Set up your `project/build.properties` to:

```
sbt.version = 0.13.5
```

> sbt 0.13.6+ has NPE problem while compiling

Set up your `build.sbt` with:

```scala
addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)

libraryDependencies += "com.wacai" %% "config-annotation" % "0.3.4"
```

## Type covenant

|Scala type | Config getter | Value      |
|-----------|---------------|------------|
| Boolean   | getBoolean    | true/false |
| Int       | getInt        | number     |
| Double    | getDouble     | float      |
| String    | getString     | text       |
| Long      | getBytes      | number with unit (B, K, M, G)       |
| +Duration | getDuration   | number with unit (ns, us, ms, s, m, h, d)|


## Integrate with akka actor

```scala
import com.wacai.config.annotation._

@conf trait kafka extends Configurable { self: Actor =>
  def config = context.system.settings.config

  ...
}
```

## Change default generation directory

Config files would be generated at `src/main/resources` as default.

It can be changed by appending macro setting to `scalacOption` in `build.sbt`:

```scala
scalacOptions += "-Xmacro-settings:conf.output.dir=/path/to/out"
```

## A runnable example

Please see [config-annotation-example][cae].


[mcr]:http://docs.scala-lang.org/overviews/macros/annotations.html
[conf]:https://github.com/typesafehub/config
[cae]:https://github.com/wacai/config-annotation-example
