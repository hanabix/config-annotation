lazy val root = (project in file("."))
  .settings(basicSettings: _*)
  .settings(dependencySettings: _*)

lazy val basicSettings = Seq(
  name := "config-annotation",
  organization := "com.wacai",
  homepage := Some(url("https://github.com/zhongl/config-annotation")),
  licenses := List(
    "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
  ),
  developers := List(
    Developer(
      "zhongl",
      "Lunfu Zhong",
      "zhong.lunfu@gmail.com",
      url("https://github.com/zhongl")
    )
  ),

  scalaVersion := "2.13.4",
  scalacOptions += "-Ymacro-annotations",
  scalacOptions += "-encoding",
  scalacOptions += "utf8",
  scalacOptions += "-feature",
  scalacOptions += "-unchecked",
  scalacOptions += "-deprecation",
  scalacOptions += "-Xmacro-settings:conf.output.dir=src/test/resources",
  scalacOptions += "-language:_"
)

lazy val dependencySettings = Seq(
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  libraryDependencies += "com.typesafe" % "config" % "1.2.1",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.9" % "test"
)
