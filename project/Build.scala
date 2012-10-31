import sbt._
import Keys._

object BuildSettings {
  import Resolvers._

  val buildOrganization = "efa"
  val buildVersion = "0.1.0-SNAPSHOT"
  val buildScalaVersion = "2.9.2"

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    resolvers ++= repos,
    scalacOptions ++= Seq ("-deprecation")
  )
} 

object Resolvers {
 val netbeansRepo = "Netbeans" at "http://bits.netbeans.org/maven2/"
 val scalatoolsRepo = "Scala-Tools Maven2 Repository Releases" at
   "http://scala-tools.org/repo-releases"
 val sonatypeRepo = "releases" at
   "http://oss.sonatype.org/content/repositories/releases"

 val repos = Seq (netbeansRepo, scalatoolsRepo, sonatypeRepo)
}

object Dependencies {
  val nbV = "RELEASE71"
  val orgNb = "org.netbeans.api"

  val scalaSwing = "org.scala-lang" % "scala-swing" % "2.9.2" 

  val react = "efa.react" %% "core" % "0.1.0-SNAPSHOT" changing

  val react_swing = "efa.react" %% "swing" % "0.1.0-SNAPSHOT" changing
 
  val nbAnnotations = orgNb % "org-netbeans-api-annotations-common" % nbV
  val nbUtil = orgNb % "org-openide-util" % nbV
  val nbLookup = orgNb % "org-openide-util-lookup" % nbV
  val nbExplorer = orgNb % "org-openide-explorer" % nbV
  val nbWindows = orgNb % "org-openide-windows" % nbV
  val nbNodes = orgNb % "org-openide-nodes" % nbV
  val nbFilesystems = orgNb % "org-openide-filesystems" % nbV
  val nbLoaders = orgNb % "org-openide-loaders" % nbV
  val nbModules = orgNb % "org-openide-modules" % nbV
  val nbAwt = orgNb % "org-openide-awt" % nbV
  val nbSettings = orgNb % "org-netbeans-modules-settings" % nbV
  val nbActions = orgNb % "org-openide-actions" % nbV
  val nbDialogs = orgNb % "org-openide-dialogs" % nbV
  val nbOutline = orgNb % "org-netbeans-swing-outline" % nbV
  val nbAutoupdateUi = orgNb % "org-netbeans-modules-autoupdate-ui" % nbV
  val nbAutoupdateServices =
    orgNb % "org-netbeans-modules-autoupdate-services" % nbV
  val nbModulesOptions = orgNb % "org-netbeans-modules-options-api" % nbV

  val scalaz_core = "org.scalaz" %% "scalaz-core" % "7.0.0-M3"
  val scalaz_effect = "org.scalaz" %% "scalaz-effect" % "7.0.0-M3"
  val scalaz_scalacheck =
    "org.scalaz" %% "scalaz-scalacheck-binding" % "7.0.0-M3"
  val scalaz_scalacheckT = scalaz_scalacheck % "test"

  val scalacheck = "org.scalacheck" %% "scalacheck" % "1.9"
  val scalacheckT = scalacheck % "test"
  val scalazCheckT = Seq(scalaz_core, scalaz_scalacheckT, scalacheckT)
  val scalazCheckET = scalazCheckT :+ scalaz_effect
}

object UtilBuild extends Build {
  import Resolvers._
  import Dependencies._
  import BuildSettings._

  def addDeps (ds: Seq[ModuleID]) =
    BuildSettings.buildSettings ++ Seq (libraryDependencies ++= ds)

  lazy val util = Project (
    "util",
    file("."),
    settings = buildSettings
  ) aggregate (core, io, nb, localDe)

  lazy val core = Project (
    "core",
    file("core"),
    settings = addDeps (Seq (nbUtil, nbLookup, scalacheck, scalaz_core,
                             scalaz_effect, scalaz_scalacheck))
  )

  lazy val io = Project (
    "io",
    file("io"),
    settings = addDeps (scalazCheckET :+ scalaSwing)
  ) dependsOn (core)

  lazy val nb = Project (
    "nb",
    file("nb"),
    settings = addDeps (scalazCheckET ++
      Seq (nbUtil, nbLookup, nbDialogs, nbNodes, nbExplorer, nbModules,
           nbFilesystems, nbLoaders, scalaSwing, react, react_swing))
  ) dependsOn (core, io)

  lazy val localDe = Project (
    "localDe",
    file("localDe"),
    settings = addDeps (scalazCheckT)
  ) dependsOn (core, nb, io)
}

// vim: set ts=2 sw=2 et:
