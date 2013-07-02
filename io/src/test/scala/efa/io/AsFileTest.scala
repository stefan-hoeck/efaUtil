package efa.io

import efa.core._, Efa._
import EfaIO._
import java.io.File
import org.scalacheck._, Prop._
import scalaz._, Scalaz._

object AsFileTest extends Properties("AsFile") {

  private lazy val fs = System.getProperty("file.separator")
  private lazy val home = System.getProperty("user.home")
  private lazy val rpg = ".efaIoTest"
  private lazy val root = home + fs + rpg
  private lazy val forbidden = "/usr/local/lib"

  lazy val nGen = Gen.identifier filter (_.trim.nonEmpty)

  lazy val logger = ∅[LoggerIO]

  def testIO[A](i: LogDisIO[A]): DisRes[A] =
    logger.logDisV(i).run.unsafePerformIO

  property("file_string") = forAll(nGen) {s ⇒ 
    testIO(s.file map (_.getPath)) ≟ s.right
  }

  property("fileInputStream_error") = forAll(nGen) {s ⇒
    testIO(s.inputStream) isLeft
  }
  
  property("createFile_String_Success") = forAll(nGen) { s ⇒ 
    val path = root + fs + s
    val res = testIO(root.mkdirs >> path.create >> path.delete)
    res ≟ ().right :| s"$res"
  }
}

// vim: set ts=2 sw=2 et:
