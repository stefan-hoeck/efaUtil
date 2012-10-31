package efa.io

import scalaz._, Scalaz._//, effect.IO
import org.scalacheck._
import efa.io.{FileIO ⇒ F}
import efa.core._, Efa._
import java.io.{InputStream, OutputStream, Writer, ByteArrayOutputStream,
  ByteArrayInputStream, CharArrayWriter, CharArrayReader}

object FileIOTest extends Properties("FileIO") with ValLogIOFunctions {
  implicit def PropBoolean (b: Boolean): Prop = Prop.propBoolean(b)
  implicit val CloseStubResource = resource[CloseStub](_.close())
  implicit val ErrorCloseStubResource = resource[ErrorCloseStub](_.close())

  private lazy val fs = System.getProperty("file.separator")
  private lazy val home = System.getProperty("user.home")
  private lazy val rpg = ".efaIoTest"
  private lazy val root = home + fs + rpg
  private lazy val forbidden = "/usr/local/lib"
  testIO (F mkdirs root)

  lazy val nGen = Gen.identifier filter (_.trim.nonEmpty)

  lazy val logger = LoggerIO.consoleLogger

  def testIO[A] (i: ValLogIO[A]): DisRes[A] =
    logger.logValV (i).run.unsafePerformIO

  property("file_string") = Prop.forAll(nGen) {s ⇒ 
    testIO (F file s map (_.getPath)) ≟ s.right
  }

  property("fileInputStream_error") = Prop.forAll(nGen) {s ⇒
    testIO (F fileInputStream s) isLeft
  }
  
  property("createFile_String_Success") = Prop.forAll(nGen) {s ⇒ {
      val path = root + fs + s
      testIO (F.createFile(path) ∗ (F.deleteFile))≟ ().right
    }
  }

  property("createFile_String_Fail") = Prop.forAll(nGen) {s ⇒ {
      val path = forbidden + fs + s
      testIO (F.createFile(path) ∗ (F.deleteFile)).isLeft
    }
  }

  property("close") = Prop.forAll { u: Unit ⇒ 
    val c = new CloseStub
    (testIO(F close c) ≟ ().right) && c.closed
  }

  property("close_error") = Prop.forAll { u: Unit ⇒ 
    val c = new ErrorCloseStub
    (testIO(F close c) ≟ ().right) && c.called
  }

  //property("close_error2") = Prop.forAll { u: Unit ⇒ 
  //  testIO(F.close[CloseStub](sys.error("buh"))) ≟ ().right
  //}

  property("withClose") = Prop.forAll(nGen) {s ⇒ 
    var createCount = 0
    var closed = false
    class Cl {
      createCount += 1
      def close() {closed = true}
    }

    implicit val ClResource = resource[Cl]{_.close()}

    val f = (c: Cl) ⇒ success(!closed)
    val res = testIO (success(new Cl) ∗ (F.withClose(_, s)(f)))

    (res ≟ true.right) :| "was closed too early" &&
    closed :| "was not closed" &&
    (createCount ≟ 1) :| "created more than once"
  }

  property("withClose_error_in_f") = Prop.forAll(nGen) {s ⇒ 
    val f: CloseStub ⇒ ValLogIO[Boolean] = _ ⇒ 
      success[Boolean](throw ExceptionStub)
    val c = new CloseStub
    val res: DisRes[Boolean] = (s + ": ").wrapNel.left[Boolean]
    val tRes: DisRes[Boolean] = testIO(F.withClose(c,s)(f))
    (tRes ≟ res) && c.closed
  }

  property("withInputStream") = Prop.forAll { u: Unit ⇒ 
    val is = new InputStreamStub
    val f = (i: InputStream) ⇒ success(!is.closed)
    (testIO (F.withInputStream(is, "")(f)) ≟ true.right) && is.closed
  }

  property("withInputStream_error_in_f") = Prop.forAll(nGen) { s ⇒ 
    val f: InputStream ⇒ ValLogIO[Boolean] = _ ⇒
      success[Boolean](throw ExceptionStub)
    val is = new InputStreamStub
    val res = (s + ": ").wrapNel.left[Boolean]
    val ioRes = testIO (F.withInputStream(is, s)(f))
    (ioRes ≟ res) && is.closed
  }

  property("withOutputStream") = Prop.forAll { u: Unit ⇒ 
    val os = new OutputStreamStub
    val f = (o: OutputStream) ⇒ success(())
    (testIO (F.withOutputStream(os, "")(f)) ≟ ().right) && os.closed
  }

  property("withOutputStream_error_in_f") = Prop.forAll(nGen) { s ⇒ 
    val f: OutputStream ⇒ ValLogIO[Unit] = _ ⇒
      success[Unit](throw ExceptionStub)
    val os = new OutputStreamStub
    val res = (s + ": ").wrapNel.left[Unit]
    (testIO (F.withOutputStream(os, s)(f)) ≟ res) && os.closed
  }

  import CharSet.UTF8

  property("withWriter") = Prop.forAll { u: Unit ⇒ 
    val os = new OutputStreamStub
    val f = (w: Writer) ⇒ success(())
    (testIO (F.withWriter(os, "", UTF8)(f)) ≟ ().right) && os.closed
  }

  property("withWriter_error_in_f") = Prop.forAll(nGen) { s ⇒ 
    val f: Writer ⇒ ValLogIO[Unit] = _ ⇒
      success[Unit](throw ExceptionStub)
    val os = new OutputStreamStub
    val res = (s + ": ").wrapNel.left[Unit]
    (testIO (F.withWriter(os, s, UTF8)(f)) ≟ res) && os.closed
  }

  property("writeAndReadTestClass") = Prop.forAll {tc: TestClass ⇒ 
    ("TestClass" xml tc).read[TestClass] ≟ tc.success
  }

  property("writeAndReadXml") = Prop.forAll {tc: TestClass ⇒ 
    val res = for {
      os <- success(new ByteArrayOutputStream(8192))
      res1 <- F.writeXml("TestClass" xml tc, os, "", UTF8)
      is <- success(new ByteArrayInputStream(os.toByteArray))
      res2 <- F.readXmlStream[TestClass](is)
    } yield { (res1 ≟ ()) && (res2 ≟ tc) }
    testIO (res) fold (_ ⇒ false, identity)
  }
}

private[io] class CloseStub {
  private var _closed = false
  def close() { _closed = true }
  def closed = _closed
}

private[io] class ErrorCloseStub {
  private var _called = false
  def close() { _called = true; throw ExceptionStub }
  def called = _called
}

private[io] object ExceptionStub extends Exception {
  override def toString = ""
}

private[io] class InputStreamStub
extends java.io.ByteArrayInputStream(Array.empty) {
  private var _closed = false
  override def close() { _closed = true; super.close() }
  def closed = _closed
}

private[io] class OutputStreamStub
extends java.io.ByteArrayOutputStream(10) {
  private var _closed = false
  override def close() { _closed = true; super.close() }
  def closed = _closed
}

// vim: set ts=2 sw=2 et:
