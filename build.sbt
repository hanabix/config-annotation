lazy val root = (project in file("."))
  .settings(basicSettings: _*)
  .settings(dependencySettings: _*)
  .settings(addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full))

lazy val basicSettings = Seq(
  name := "config-annotation",
  organization := "com.wacai",
  scalaVersion := "2.11.5",
  crossScalaVersions := Seq(scalaVersion.value, "2.12.0"),
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
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)



// ~/bin/herald --publish
