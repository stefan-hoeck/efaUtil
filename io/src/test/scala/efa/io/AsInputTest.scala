package efa.io

import efa.core._, Efa._
import java.io.File
import org.scalacheck._, Prop._
import scalaz._, Scalaz._, scalaz.std.indexedSeq._

object AsInputTest extends Properties("AsInput") {
  val BI = AsInput[Bytes]

  val logger = ∅[LoggerIO]

  def testIO[A] (i: LogToDisIO[A]): DisRes[A] =
    (i run logger).run.unsafePerformIO

  property("allBytes") = forAll { bytes: List[Byte] ⇒ 
    val bs = Bytes(bytes.toArray)

    testIO(BI allBytes bs map { _.toList}) ≟ bytes.right
  }

  property("allBytes_close") = forAll { bytes: List[Byte] ⇒ 
    val bs = Bytes(bytes.toArray)

    testIO(BI allBytes bs map { _.toList})

    bs.isClosed
  }

  property("allLines") = forAll(Gen listOf Gen.identifier) { ls ⇒ 
    val bs = Bytes(ls mkString "\n")

    testIO(BI allLines bs) ≟ ls.toIndexedSeq.right
  }

  property("readXml_error") = forAll { bytes: List[Byte] ⇒ 
    val bs = Bytes(bytes.toArray)

    testIO(BI.readXml[String](bs)).isLeft
  }

  property("readXml_success") = forAll(Gen.identifier) { s ⇒ 
    val bs = Bytes(s"<name>${s}</name>")

    testIO(BI.readXml[String](bs)) ≟ s.right
  }

  property("readXml_fail") = forAll(Gen.identifier) { s ⇒ 
    val bs = Bytes(s"<name>${s}</name>")

    testIO(BI.readXml[Int](bs)).isLeft
  }

  property("copy") = forAll { bytes: List[Byte] ⇒ 
    val out = new BytesOut
    val in = Bytes(bytes.toArray)

    val res = testIO(BI copy (in, out))

    res.isRight :| "copying successful" &&
    out.isClosed :| "closed out" &&
    in.isClosed :| "closed in" &&
    (out.toByteArray.toList ≟ bytes) :| "actually copied data"
  }

}

// vim: set ts=2 sw=2 et:
