package efa.io

import scalaz._, Scalaz._//, effect.IO
import org.scalacheck._, Prop._
import efa.core._, Efa._
import java.io.File

object AsFileTest extends Properties("AsFile") with AsFileInstances {

  //private lazy val fs = System.getProperty("file.separator")
  //private lazy val home = System.getProperty("user.home")
  //private lazy val rpg = ".efaIoTest"
  //private lazy val root = home + fs + rpg
  //private lazy val forbidden = "/usr/local/lib"

  //lazy val nGen = Gen.identifier filter (_.trim.nonEmpty)

  //lazy val logger = LoggerIO.consoleLogger

  //private val AF: AsFile[String] = implicitly

  //def testIO[A] (i: ValLogIO[A]): DisRes[A] =
  //  logger.logValV (i).run.unsafePerformIO

  //property("file_string") = forAll(nGen) {s ⇒ 
  //  testIO(AF file s map (_.getPath)) ≟ s.right
  //}

  //property("fileInputStream_error") = forAll(nGen) {s ⇒
  //  testIO(AF inputStream s) isLeft
  //}
  //
  //property("createFile_String_Success") = forAll(nGen) { s ⇒ 
  //  val path = root + fs + s
  //  testIO((AF create path) >> (AF delete path))≟ ().right
  //}
}

// vim: set ts=2 sw=2 et:
