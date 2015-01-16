
lazy val root = (project in file("."))
  .settings(
    name := "config-annotation",
    organization := "com.wacai",
    version := "0.1",
    scalaVersion := "2.11.5",
    crossScalaVersions := Seq("2.10.2", "2.10.3", "2.10.4", "2.11.0", "2.11.1", "2.11.2", "2.11.3", "2.11.4", "2.11.5"),
    scalacOptions ++= Seq(
      "-encoding", "utf8",
      "-feature",
      "-unchecked",
      "-deprecation",
      "-language:_",
//      "-Ymacro-debug-verbose",
      "-Xlog-reflective-calls"
    ),
    unmanagedSourceDirectories in Compile <+= (sourceDirectory in Compile, scalaBinaryVersion) {
      (sourceDir, version) => sourceDir / (if (version.startsWith("2.10")) "scala_2.10" else "scala_2.11")
    },
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full),
    libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _),
    libraryDependencies += "com.typesafe" % "config" % "1.2.1" withSources(),
    libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test" withSources()
  )



val scalatest = "org.scalatest" % "scalatest_2.11" % "2.2.1"
    