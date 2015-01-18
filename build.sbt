
lazy val root = (project in file("."))
  .settings(basicSettings: _*)
  .settings(dependencySettings: _*)
  .settings(publishSettings: _*)
  .settings(sbtrelease.ReleasePlugin.releaseSettings: _*)
  .settings(addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full))



lazy val basicSettings = Seq(
  name := "config-annotation",
  organization := "com.wacai",
  scalaVersion := "2.11.5",
  scalacOptions ++= Seq("-encoding", "utf8", "-feature", "-unchecked", "-deprecation", "-language:_")
)

lazy val dependencySettings = Seq(
  libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _),
  libraryDependencies += "com.typesafe" % "config" % "1.2.1",
  libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)

lazy val publishSettings = Seq(
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { (repo: MavenRepository) => false},
  pomExtra := pomXml) ++ xerial.sbt.Sonatype.sonatypeSettings

lazy val pomXml = {
  <url>https://github.com/wacai/config-annotation</url>
    <licenses>
      <license>
        <name>Apache License 2.0</name>
        <url>http://www.apache.org/licenses/</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>git@github.com:wacai/config-annotation.git</url>
      <connection>scm:git:git@github.com:wacai/config-annotation.git</connection>
    </scm>
    <developers>
      <developer>
        <id>zhongl</id>
        <name>zhongl</name>
        <url>http://github.com/zhongl</url>
      </developer>
    </developers>
}
