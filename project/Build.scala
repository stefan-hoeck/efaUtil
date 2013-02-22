import sbt._
import Keys._
import com.typesafe.sbt.osgi.SbtOsgi._

object BuildSettings {
  import Resolvers._

  val sv = "2.10.0"
  val buildOrganization = "efa"
  val buildVersion = "0.2.1-SNAPSHOT"
  val buildScalaVersion = sv

  val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    resolvers ++= repos,
    publishTo := Some(Resolver.file("file", 
      new File(Path.userHome.absolutePath+"/.m2/repository"))),
    scalacOptions ++= Seq ("-deprecation", "-feature",
      "-language:postfixOps", "-language:implicitConversions",
      "-language:higherKinds")
  ) ++ osgiSettings
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
  import BuildSettings.sv

  val nbV = "RELEASE71"
  val orgNb = "org.netbeans.api"

  val scalaSwing = "org.scala-lang" % "scala-swing" % sv

  val react = "efa.react" %% "react-core" % "0.2.0-SNAPSHOT" changing

  val react_swing = "efa.react" %% "react-swing" % "0.2.0-SNAPSHOT" changing
 
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
  val nbOptions = "org.netbeans.api" % "org-netbeans-modules-options-api" % nbV
  val nbAutoupdateUi = orgNb % "org-netbeans-modules-autoupdate-ui" % nbV
  val nbAutoupdateServices =
    orgNb % "org-netbeans-modules-autoupdate-services" % nbV
  val nbModulesOptions = orgNb % "org-netbeans-modules-options-api" % nbV

  val shapeless = "com.chuusai" %% "shapeless" % "1.2.3"
  val scalaz_core = "org.scalaz" %% "scalaz-core" % "7.0.0-M8"
  val scalaz_effect = "org.scalaz" %% "scalaz-effect" % "7.0.0-M8"
  val scalaz_scalacheck = "org.scalaz" %% "scalaz-scalacheck-binding" % "7.0.0-M8"

  val scalacheck = "org.scalacheck" %% "scalacheck" % "1.10.0"
  val scalacheckT = scalacheck % "test"
  val scalazCheckT = Seq(scalaz_core, scalaz_effect, scalaz_scalacheck, scalacheckT, shapeless)

  val scalazCheckET = scalazCheckT :+ scalaz_effect
}

object UtilBuild extends Build {
  import Resolvers._
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
  ) aggregate (core, io, nb, localDe)

  lazy val core = Project (
    "efa-core",
    file("core"),
    settings = addDeps(
      Seq (nbUtil, nbLookup, scalacheck, scalaz_core,
          scalaz_effect, scalaz_scalacheck, shapeless),
      Seq("efa.core.*")
    )
  )

  lazy val io = Project (
    "efa-io",
    file("io"),
    settings = addDeps(scalazCheckET :+ scalaSwing, Seq("efa.io.*"))
  ) dependsOn (core)

  lazy val nb = Project (
    "efa-nb",
    file("nb"),
    settings = addDeps(scalazCheckET ++
      Seq (nbUtil, nbLookup, nbDialogs, nbNodes, nbExplorer, nbModules,
           nbOptions, nbFilesystems, nbLoaders, scalaSwing, react, react_swing,
           shapeless), 
      Seq("efa.nb.*"))
  ) dependsOn (core, io)

  lazy val localDe = Project (
    "efa-localDe",
    file("localDe"),
    settings = addDeps(scalazCheckT, Nil)
  ) dependsOn (core, nb, io)
}

// vim: set ts=2 sw=2 et:
