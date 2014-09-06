import sbt._
import Keys._

object BuildSettings {
  val sv                = "2.11.2"
  val buildOrganization = "efa"
  val buildVersion      = "0.2.3-SNAPSHOT"
  val buildScalaVersion = sv
  val netbeansRepo      = "Netbeans" at "http://bits.netbeans.org/maven2/"

  val buildSettings = Seq(
    organization       := buildOrganization,
    version            := buildVersion,
    scalaVersion       := buildScalaVersion,
    resolvers          += netbeansRepo,
    publishTo          := Some(Resolver.file("file", 
      new File(Path.userHome.absolutePath+"/.m2/repository"))),

    scalacOptions      ++= Seq(
      "-unchecked",
      "-deprecation",
      "-feature",
      "-language:postfixOps",
      "-language:implicitConversions",
      "-language:higherKinds"
    )
  )
} 

object Dependencies {
  import BuildSettings.sv

  val nbV               = "RELEASE80"
  val scalacheckV       = "1.11.4"
  val scalazV           = "7.1.0"
  val shapelessV        = "2.0.0"
  val scalaXmlV         = "1.0.2"
  val contribV          = "0.3"

  val nb                = "org.netbeans.api"
  val scalaz            = "org.scalaz"
  val typelevel         = "org.typelevel"

  val nbUtil            = nb % "org-openide-util" % nbV
  val nbLookup          = nb % "org-openide-util-lookup" % nbV

  val scalaXml          = "org.scala-lang.modules" %% "scala-xml" % scalaXmlV
  val shapeless         = "com.chuusai" %% "shapeless" % shapelessV
  val scalaz_core       = scalaz %% "scalaz-core" % scalazV
  val scalaz_effect     = scalaz %% "scalaz-effect" % scalazV
  val scalaz_iteratee   = scalaz %% "scalaz-iteratee" % scalazV
  val scalaz_scalacheck = scalaz %% "scalaz-scalacheck-binding" % scalazV
  val shapeless_scalaz  = typelevel %% "shapeless-scalaz" % contribV
  val shapeless_check   = typelevel %% "shapeless-scalacheck" % contribV

  val scalacheck        = "org.scalacheck" %% "scalacheck" % scalacheckV
  val scalacheckT       = scalacheck % "test"
  val reflect           = "org.scala-lang" % "scala-reflect" % sv

  val deps              = Seq(scalaz_core, scalaz_effect, scalaz_scalacheck,
                              shapeless, reflect, scalaXml, shapeless_scalaz,
                              shapeless_check)
}

object UtilBuild extends Build {
  import Dependencies._
  import BuildSettings._

  def addDeps (ds: ModuleID*) =
    BuildSettings.buildSettings :+
      (libraryDependencies ++= (deps ++ ds))

  lazy val util = Project(
    "efa-util",
    file("."),
    settings = buildSettings
  ) aggregate (core, io, localDe)

  lazy val core = Project(
    "efa-core",
    file("core"),
    settings = addDeps(nbUtil, nbLookup, scalacheck)
  )

  lazy val io = Project(
    "efa-io",
    file("io"),
    settings = addDeps(scalacheckT, scalaz_iteratee)
  ) dependsOn (core)

  lazy val localDe = Project(
    "efa-localDe",
    file("localDe"),
    settings = addDeps(scalacheck)
  ) dependsOn (core, io)
}

// vim: set ts=2 sw=2 nowrap et:
