package efa.io

import efa.core._, Efa._
import EfaIO._
import java.io.File
import org.scalacheck._, Prop._
import scalaz._, Scalaz._, scalaz.std.indexedSeq._

object AsInputTest extends Properties("AsInput") {
  val logger = ∅[LoggerIO]

  def testIO[A](i: LogDisIO[A]): DisRes[A] =
    (i run logger).run.unsafePerformIO

  property("allBytes") = forAll { bytes: List[Byte] ⇒ 
    val bs = Bytes(bytes.toArray)

    testIO(bs.allBytes map { _.toList}) ≟ bytes.right
  }

  property("allBytes_close") = forAll { bytes: List[Byte] ⇒ 
    val bs = Bytes(bytes.toArray)

    testIO(bs.allBytes map { _.toList})

    bs.isClosed
  }

  property("allLines") = forAll(Gen listOf Gen.identifier) { ls ⇒ 
    val bs = Bytes(ls mkString "\n")

    testIO(bs.allLines) ≟ ls.toIndexedSeq.right
  }

  property("readXml_error") = forAll { bytes: List[Byte] ⇒ 
    val bs = Bytes(bytes.toArray)

    testIO(bs.readXml[String]).isLeft
  }

  property("readXml_success") = forAll(Gen.identifier) { s ⇒ 
    val bs = Bytes(s"<name>${s}</name>")

    testIO(bs.readXml[String]) ≟ s.right
  }

  property("readXml_fail") = forAll(Gen.identifier) { s ⇒ 
    val bs = Bytes(s"<name>${s}</name>")

    testIO(bs.readXml[Int]).isLeft
  }

  property("copy") = forAll { bytes: List[Byte] ⇒ 
    val out = new BytesOut
    val in = Bytes(bytes.toArray)

    val res = testIO(in copyTo out)

    res.isRight :| "copying successful" &&
    out.isClosed :| "closed out" &&
    in.isClosed :| "closed in" &&
    (out.toByteArray.toList ≟ bytes) :| "actually copied data"
  }

}

// vim: set ts=2 sw=2 et:
