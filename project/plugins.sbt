resolvers += Resolver.typesafeRepo("releases")

// Tox4j support libraries.
resolvers += "Tox4j snapshots" at "https://tox4j.github.io/repositories/snapshots/"
resolvers += Resolver.sonatypeRepo("snapshots")

// The Play plugin.
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.0")

// Common tox4j plugins.
addSbtPlugin("im.tox" % "build-basic" % "0.1-SNAPSHOT")

libraryDependencies ++= Seq(
  "org.json4s" %% "json4s-native" % "3.3.0",
  "com.github.os72" % "protoc-jar" % "3.0.0-b1"
)

// Force update of tox4j plugins and libraries.
//val _ = Process(Seq("rm", "-rf", sys.env("HOME") + "/.ivy2/cache/im.tox")).run().exitValue()
