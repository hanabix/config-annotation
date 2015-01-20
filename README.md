## Config annotation

[![Build Status](https://travis-ci.org/wacai/config-annotation.png?branch=master)](https://travis-ci.org/wacai/config-annotation)
[![Codacy Badge](https://www.codacy.com/project/badge/b9158949586c439cb05e21333f52798b)](https://www.codacy.com/public/zhonglunfu/config-annotation)

Using scala [macro annotation][mcr] to help loading [config][conf].

## Example

`KafkaBroker.scala`:

```
trait KafkaBroker {
  val host: String
  val port: Int
  val soTimeout: Duration
  val bufferSize: Long
  val clientId: String
}
```

`KafkaConsumer.scala`:

```
import com.wacai.config.annotation._

@conf[KafkaBroker] class KafkaConsumer extends Actor {
  val client = new SimpleConsumer(host, port, soTimeout, bufferSize, clientId)

  def receive = ???
}
```

`application.conf`:

```

kafka_broker {
  host = wacai.com
  port = 12306
  so.timeout = 5s
  buffers.size = 64k
  client.id = wacai
}
```

`@conf` will let scala compile to insert codes to `KafkaConsumer`:

```
class KafkaConsumer extends Actor with KafkaBroker {
  val host = config.getString("kafka.host")
  val port = config.getInt("kafka.port")
  val soTimeout = Duration(config.getDuration("kafka.so.timeout", SECONDS))
  val bufferSize = config.getBytes("kafka.buffer.size")
  val clientId = config.getString("kafka.client.id")

  ...
}
```

> Caution: IDE would report error, because macro has not be supported yet.

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

libraryDependencies += "com.wacai" %% "config-annotation" % "0.2.0"
```

## Path covenant

|Scala definition | Config path |
|-----------------|-------------|
|KafkaBroker.bufferSize | kafka_broker.buffer.size|

## Type covenant

|Scala type | Config getter |
|-----------|---------------|
| Boolean   | getBoolean    |
| Int       | getInt        |
| Long      | getBytes      |
| Double    | getDouble     |
| String    | getString     |
| Duration  | getDuration   |


## Integrate with akka actor

```
import com.wacai.config.annotation._

@conf[Settings] class MyActor extends Actor with Configurable {
  val config = context.system.settings.config

  def receive = ???

}
```

## More detail

Please see test cases.

## Early release

[v0.1.2](https://github.com/wacai/config-annotation/tree/v0.1.2)

[mcr]:http://docs.scala-lang.org/overviews/macros/annotations.html
[conf]:https://github.com/typesafehub/config
