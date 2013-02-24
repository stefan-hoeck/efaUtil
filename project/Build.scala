import sbt._
import Keys._
import com.typesafe.sbt.osgi.SbtOsgi._

object BuildSettings {
  val sv = "2.10.0"
  val buildOrganization = "efa"
  val buildVersion = "0.2.1-SNAPSHOT"
  val buildScalaVersion = sv
  val netbeansRepo = "Netbeans" at "http://bits.netbeans.org/maven2/"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    resolvers += netbeansRepo,
    publishTo := Some(Resolver.file("file", 
      new File(Path.userHome.absolutePath+"/.m2/repository"))),
    scalacOptions ++= Seq ("-deprecation", "-feature",
      "-language:postfixOps", "-language:implicitConversions",
      "-language:higherKinds")
  ) ++ osgiSettings
} 

object Dependencies {
  import BuildSettings.sv

  val nbV = "RELEASE71"
  val reactV = "0.2.1-SNAPSHOT"
  val scalazV = "7.0.0-M8"

  val nb = "org.netbeans.api"
  val react = "efa.react"
  val scalaz = "org.scalaz"

  val scalaSwing = "org.scala-lang" % "scala-swing" % sv

  val react_core = react %% "react-core" % reactV changing

  val react_swing = react %% "react-swing" % reactV changing

  val osgi_core = "org.osgi" % "org.osgi.core" % "4.2.0" % "provided"

  val nbUtil = nb % "org-openide-util" % nbV
  val nbLookup = nb % "org-openide-util-lookup" % nbV

  val shapeless = "com.chuusai" %% "shapeless" % "1.2.3"
  val scalaz_core = scalaz %% "scalaz-core" % scalazV
  val scalaz_effect = scalaz %% "scalaz-effect" % scalazV
  val scalaz_scalacheck = scalaz %% "scalaz-scalacheck-binding" % scalazV

  val scalacheck = "org.scalacheck" %% "scalacheck" % "1.10.0"
  val scalacheckT = scalacheck % "test"

  val coolness = Seq(scalaz_core, scalaz_effect, scalaz_scalacheck, shapeless)
}

object UtilBuild extends Build {
  import Dependencies._
  import BuildSettings._

  def addDeps (ds: Seq[ModuleID], exports: Seq[String]) =
    BuildSettings.buildSettings ++ Seq(
      libraryDependencies ++= ds,
      OsgiKeys.exportPackage := exports
    )

  lazy val util = Project (
    "efa-util",
    file("."),
    settings = buildSettings
  ) aggregate (core, io, localDe)

  lazy val core = Project (
    "efa-core",
    file("core"),
    settings = addDeps(
      Seq (nbUtil, nbLookup, scalacheck) ++ coolness,
      Seq("efa.core.*")
    )
  )

  lazy val io = Project (
    "efa-io",
    file("io"),
    settings = addDeps(
      Seq(scalacheckT, scalaSwing) ++ coolness,
      Seq("efa.io.*")
    )
  ) dependsOn (core)

  lazy val localDe = Project (
    "efa-localDe",
    file("localDe"),
    settings = addDeps(
      scalacheck +: coolness, 
      Seq("efa.local.de") //needed for netbeans
    )
  ) dependsOn (core, io)
}

// vim: set ts=2 sw=2 nowrap et:
