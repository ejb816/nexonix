ThisBuild / organization := "org.nexonix"
ThisBuild / version := "1.1.1-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.14"
publishMavenStyle := true

enablePlugins(JavaAppPackaging)

// Use Apache Ivy; necessary to store
//   dependencies in lib directory.
//ThisBuild / useCoursier := true
lazy val dependencies =
  new {
    val circeVersion = "0.14.1"
    val evreteVersion = "4.0.3"
    val pekkoActorVersion = "1.2.0-M1"
    val scalaTestVersion = "3.2.15"
    val scalaVersion = "2.13.10"
    val scalaSwingVersion = "2.1.1"
    val jlineVersion = "3.22.0"
    val pekkoActorTyped = "org.apache.pekko" %% "pekko-actor-typed" % pekkoActorVersion
    val pekkoActorTestkitTyped = "org.apache.pekko" %% "pekko-actor-testkit-typed" % pekkoActorVersion
    val slf4j = "org.slf4j" % "slf4j-jdk14" % "2.0.9" % Test
    val circeCore = "io.circe" %% "circe-core" % circeVersion
    val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
    val circeParser = "io.circe" %% "circe-parser" % circeVersion
    val circeOptics =  "io.circe" %% "circe-optics" % circeVersion
    val circeYaml = "io.circe" %% "circe-yaml" % circeVersion
    val evreteCore = "org.evrete" % "evrete-core" % evreteVersion
    val evreteJavaDsl = "org.evrete" % "evrete-dsl-java" % evreteVersion
    val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % Test
    val scalaReflect = "org.scala-lang" % "scala-reflect" % scalaVersion
    val scalaCompiler = "org.scala-lang" % "scala-compiler" % scalaVersion

    val scalaSwing = "org.scala-lang.modules" %% "scala-swing" % scalaSwingVersion
    val jline = "org.jline" % "jline" % jlineVersion
  }

lazy val root = (project in file("."))
  .settings(
    name := "nexonix-draco",

    /**
     * sbt-native-packager config - creates package for deploying
     */
     Compile / mainClass := Some("org.mitre.anvil.rules.DataDictionaryMap"),
     Compile / discoveredMainClasses := Seq(),

    libraryDependencies ++= Seq(
      dependencies.pekkoActorTyped,
      dependencies.pekkoActorTestkitTyped,
      dependencies.slf4j,
      dependencies.circeCore,
      dependencies.circeGeneric,
      dependencies.circeParser,
      dependencies.circeYaml,
      dependencies.circeOptics,
      dependencies.evreteCore,
      dependencies.evreteJavaDsl,
      //dependencies.evreteJsr94,
      dependencies.scalaTest,
      dependencies.scalaReflect,
      dependencies.scalaSwing,
      dependencies.scalaCompiler
    )
  )

ThisBuild / managedScalaInstance := false

// Add the configuration for the dependencies on Scala tool jars
// You can also use a manually constructed configuration like:
//   config("scala-tool").hide
ivyConfigurations += Configurations.ScalaTool

// Add the usual dependency on the library as well on the compiler in the
//  'scala-tool' configuration
