![ci badge](https://github.com/zhongl/config-annotation/workflows/CI/badge.svg) ![release badge](https://github.com/zhongl/config-annotation/workflows/Release/badge.svg) [![Maven Central](https://img.shields.io/maven-central/v/com.wacai/config-annotation_2.13)](https://search.maven.org/artifact/com.wacai/config-annotation_2.13) [![Coverage Status](https://coveralls.io/repos/github/zhongl/config-annotation/badge.svg?branch=master)](https://coveralls.io/github/zhongl/config-annotation?branch=master)

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

Set up your `build.sbt` with:

### Scala 2.11

```scala
addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)

libraryDependencies += "com.wacai" %% "config-annotation" % "0.3.5"
```

### Scala 2.12

```scala
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

libraryDependencies += "com.wacai" %% "config-annotation" % "0.3.6"
```

### Scala 2.13

```scala
libraryDependencies += "com.wacai" %% "config-annotation" % "0.4.0"
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
