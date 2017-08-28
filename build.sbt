import sbt._
import Keys._
import Dependencies._

val buildOrganization = "efa"
val buildVersion      = "0.3.0"
val buildScalaVersion = sv
val netbeansRepo      = "Netbeans" at "http://bits.netbeans.org/maven2/"

val buildSettings = Seq(
  organization       := buildOrganization,
  version            := buildVersion,
  scalaVersion       := buildScalaVersion,
  resolvers          += netbeansRepo,
  publishTo          := Some(Resolver.file("file", 
    new File(Path.userHome.absolutePath+"/.m2/repository"))),
  libraryDependencies in ThisBuild ++= deps,

  scalacOptions      ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-language:postfixOps",
    "-language:implicitConversions",
    "-language:higherKinds"
  )
)


lazy val util = Project("efa-util", file("."))
                  .settings(buildSettings)
                  .aggregate(core, io, localDe)

lazy val core = Project("efa-core", file("core"))
                  .settings(
                    buildSettings,
                    libraryDependencies ++= Seq(nbUtil, nbLookup, scalacheck)
                  )

lazy val io = Project("efa-io", file("io"))
                  .settings(
                    buildSettings,
                    libraryDependencies ++= Seq(scalacheckT, scalaz_iteratee)
                  ).dependsOn(core)

lazy val localDe = Project("efa-localDe", file("localDe"))
                  .settings(
                    buildSettings,
                    libraryDependencies ++= Seq(scalacheck)
                  ).dependsOn(core, io)

// vim: set ts=2 sw=2 nowrap et:
