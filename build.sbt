name := "scala-bot"

version := "0.1"

scalaVersion := "2.12.9"

// Core with minimal dependencies, enough to spawn your first bot.
libraryDependencies ++= Seq(
  "com.bot4s" %% "telegram-core" % "4.4.0-RC2",
  "com.softwaremill.sttp" %% "json4s" % "1.7.2",
  "org.json4s" %% "json4s-native" % "3.6.0",
  "org.scalatest" %% "scalatest" % "3.1.1" % Test
)