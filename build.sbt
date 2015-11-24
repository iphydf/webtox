organization := "im.tox"
name         := "webtox"
scalaVersion := "2.11.7"

import com.trueaccord.scalapb.{ScalaPbPlugin => PB}
import sbt.tox4j._
import sbt.tox4j.lint._

PB.protobufSettings
PB.javaConversions in PB.protobufConfig := true
PB.flatPackage in PB.protobufConfig := true

CodeFormat.projectSettings
Scalastyle.moduleSettings
WebJarsSymlinks.projectSettings

lazy val root = (project in file(".")).enablePlugins(PlayScala)

// Add resolvers.
resolvers += "Tox4j snapshots" at "https://tox4j.github.io/repositories/snapshots/"
resolvers += Resolver.bintrayRepo("webjars", "maven")
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  // Play modules.
  jdbc, ws,

  // Database access.
  "com.typesafe.play" %% "anorm" % "2.4.0",

  // JSON formatters for protobufs.
  "com.googlecode.protobuf-java-format" % "protobuf-java-format" % "1.4",

  // Date/time libraries.
  "codes.reactive" %% "scala-time" % "0.3.0-SNAPSHOT",

  // WebJars helpers.
  "org.webjars" %% "webjars-play" % "2.4.0-2",

  // Required WebJars.
  "org.webjars.bower" % "github-com-PolymerElements-iron-elements" % "[1.0.0,2)",
  "org.webjars.bower" % "github-com-PolymerElements-molecules" % "[1.0.0,2)",
  "org.webjars.bower" % "github-com-PolymerElements-neon-elements" % "[1.0.0,2)",
  "org.webjars.bower" % "github-com-PolymerElements-paper-elements" % "[1.0.0,2)",
  "org.webjars.bower" % "github-com-PolymerElements-platinum-elements" % "[1.0.0,2)",
  "org.webjars.bower" % "jquery" % "2.1.4",
  "org.webjars.bower" % "moment" % "2.10.6",
  "org.webjars.bower" % "requirejs" % "2.1.20",
  "org.webjars.bower" % "webcomponentsjs" % "0.7.18",

  // Tox4j.
  organization.value %% "tox4j" % version.value
)
