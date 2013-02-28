package efa.io

import efa.core._, Efa._
import java.io.File
import org.scalacheck._, Prop._
import scalaz._, Scalaz._, scalaz.std.indexedSeq._

object AsOutputTest extends Properties("AsOutput") {
  val BI = AsInput[Bytes]
  val BO = AsOutput[BytesOut]

  val logger = ∅[LoggerIO]

  def testIO[A] (i: LogToDisIO[A]): DisRes[A] =
    (i run logger).run.unsafePerformIO

  property("linesOut") = forAll(Gen listOf Gen.identifier) { lines ⇒ 
    val in = Bytes(lines)
    val out = new BytesOut

    testIO(BO.linesOut(out) &= BI.lines(in) run)
    val res = out.getLines

    res ≟ lines :| s"Exp: $lines, found: $res"
  }
}

// vim: set ts=2 sw=2 et:
