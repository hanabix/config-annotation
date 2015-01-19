## Config annotation

[![Build Status](https://travis-ci.org/wacai/config-annotation.png?branch=master)](https://travis-ci.org/wacai/config-annotation)

Using scala [macro annotation][mcr] to help loading [config][conf].

## Example

`Kafka.scala`:

```
trait Kafka extends Configurable {
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

@conf[Kafka] class KafkaConsumer extends Actor {
  val client = new SimpleConsumer(host, port, soTimeout, bufferSize, clientId)

  def receive = ???
}
```

`application.conf`:

```

kafka {
  host = wacai.com
  port = 12306
  so.timeout = 5s
  buffers.size = 64k
  client.id = wacai
}
```

`@conf` will let scala compile to insert codes to `KafkaConsumer`:

```
class KafkaConsumer extends Actor with Kafka {
  val host = config.getString("kafka.host")
  val port = config.getInt("kafka.port")
  val soTimeout = Duration(config.getDuration("kafka.so.timeout", SECONDS))
  val bufferSize = config.getBytes("kafka.buffer.size")
  val clientId = config.getString("kafka.client.id")

  ...
}
```

## Installation

Set up your `project/build.properties` to:

```
sbt.version = 0.13.5
```

> sbt 0.13.6+ has NPE problem while compiling

Set up your `build.sbt` with:

```
addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += "com.wacai" %% "config-annotation" % "0.1-SNAPSHOT"

```

## Path covenant

|Scala definition | Config path |
|-----------------|-------------|
|Server.port      | server.port  |
|HttpServer.port  | http_server.port|
|Server.maxBufferSize| server.max.buffer.size|

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

class MyActor extends Actor with Configurable {
  val config = context.system.settings.config

  @conf val threshold = 0

  def receive = ???

}
```

## More detail

Please see test cases.


[mcr]:http://docs.scala-lang.org/overviews/macros/annotations.html
[conf]:https://github.com/typesafehub/config