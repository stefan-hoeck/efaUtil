import sbt._
import Keys._

object Dependencies {
  val sv                = "2.12.3"

  val nbV               = "RELEASE80"
  val scalacheckV       = "1.12.5"
  val scalazV           = "7.2.15"
  val shapelessV        = "2.3.2"
  val scalaXmlV         = "1.0.5"

  val nb                = "org.netbeans.api"
  val scalaz            = "org.scalaz"

  val nbUtil            = nb % "org-openide-util" % nbV
  val nbLookup          = nb % "org-openide-util-lookup" % nbV

  val scalaXml          = "org.scala-lang.modules" %% "scala-xml" % scalaXmlV
  val shapeless         = "com.chuusai" %% "shapeless" % shapelessV
  val scalaz_core       = scalaz %% "scalaz-core" % scalazV
  val scalaz_effect     = scalaz %% "scalaz-effect" % scalazV
  val scalaz_iteratee   = scalaz %% "scalaz-iteratee" % scalazV
  val scalaz_scalacheck = scalaz %% "scalaz-scalacheck-binding" % scalazV

  val scalacheck        = "org.scalacheck" %% "scalacheck" % scalacheckV
  val scalacheckT       = scalacheck % "test"
  val reflect           = "org.scala-lang" % "scala-reflect" % sv

  val deps              = Seq(scalaz_core, scalaz_effect, scalaz_scalacheck,
                              shapeless, reflect, scalaXml)
}

// vim: set ts=2 sw=2 nowrap et:
