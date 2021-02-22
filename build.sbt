lazy val root = (project in file("."))
  .settings(basicSettings: _*)
  .settings(
    libraryDependencies ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n <= 12 =>
	  List(compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full))
	case _ => Nil
      }
    },
    Compile / scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n <= 12 => Nil
	case _                       => List("-Ymacro-annotations")
      }
    }
  )
  .settings(dependencySettings: _*)

lazy val basicSettings = Seq(
  name := "config-annotation",
  organization := "com.wacai",
  scalaVersion := "2.13.4",
  crossScalaVersions := Seq(scalaVersion.value, "2.12.10", "2.11.12"),
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



// ~/bin/herald --publish
