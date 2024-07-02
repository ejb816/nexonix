ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "nexonix"
  )

val circeVersion = "0.14.1"
val evreteVersion = "3.0.02"
val akkaVersion = "2.7.0"
val slf4jVersion = "2.0.7"
libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.2.15",
  "org.scalatest" %% "scalatest" % "3.2.15" % "test"
)
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed",
  "com.typesafe.akka" %% "akka-actor-testkit-typed"
).map(_ % akkaVersion)

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api",
  "org.slf4j" % "slf4j-simple"
).map(_ % slf4jVersion)

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-optics"
).map(_ % circeVersion)
libraryDependencies ++= Seq(
  "org.evrete" % "evrete-core",
  "org.evrete" % "evrete-dsl-java",
  "org.evrete" % "evrete-jsr94"
).map(_ % evreteVersion)
