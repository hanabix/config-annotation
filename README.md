## Config annotation

[![Build Status](https://travis-ci.org/wacai/config-annotation.png?branch=master)](https://travis-ci.org/wacai/config-annotation)
[![Codacy Badge](https://www.codacy.com/project/badge/b9158949586c439cb05e21333f52798b)](https://www.codacy.com/public/zhonglunfu/config-annotation)

Using scala [macro annotation][mcr] to mark a [config][conf] style `trait` for mapping items from [config][conf] file.

## Example

`kafka.scala`:

```
import com.wacai.config.annotation._

import scala.concurrent.duration._

@conf trait kafka {
  val server = new {
    val host = "localhost"
    val port = 9092
  }

  val socket = new {
    val timeout = 3s
    val buffer = 1024 * 64
  }

  val client = "id"
}
```

`KafkaConsumer.scala`:

```
class KafkaConsumer extends kafka {
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

`application.conf`:

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

`@conf` will let scala compile to insert codes to `kafka.scala`:

```
trait kafka {
  val server = new {
    val host = config.getString("kafka.host")
    val port = config.getInt("kafka.port")
  }
  val socket = new {
    val timeout = Duration(config.getDuration("kafka.socket.timeout", SECONDS))
    val buffer = config.getBytes("kafka.socket.buffer")
  }
  val client = config.getString("kafka.client")

  ...
}
```

## Installation

> Caution: only support scala 2.11.0+

Set up your `project/build.properties` to:

```
sbt.version = 0.13.5
```

> sbt 0.13.6+ has NPE problem while compiling

Set up your `build.sbt` with:

```
addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)

libraryDependencies += "com.wacai" %% "config-annotation" % "0.3.0"
```

## Type covenant

|Scala type | Config getter |
|-----------|---------------|
| Boolean   | getBoolean    |
| Int       | getInt        |
| Long      | getBytes      |
| Double    | getDouble     |
| String    | getString     |
| +Duration | getDuration   |


## Integrate with akka actor

```
import com.wacai.config.annotation._

@conf trait kafka extends Configurable { self: Actor =>
  def config = context.system.settings.config

  ...
}
```

## More detail

Please see test cases.


[mcr]:http://docs.scala-lang.org/overviews/macros/annotations.html
[conf]:https://github.com/typesafehub/config
