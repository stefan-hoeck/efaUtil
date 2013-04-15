package efa.io

import org.scalacheck._, Prop._
import scalaz._, Scalaz._

object IOChooserTest extends Properties("IOChooser") {
  import IOChooser.adjustEnding

  property("adjustEnding_empty") = forAll {s : String ⇒ 
    adjustEnding(Nil)(s) ≟ s
  }

  val pGen = for {a ← Gen.identifier; b ← Gen.identifier} yield (a,b)
  property("adjustEnding_existing") = forAll(pGen) {p ⇒ 
    val (a, b) = p
    val conc = a + "." + b
    adjustEnding(Seq(b))(conc) ≟ conc 
  }

  property("adjustEnding_nonExisting") = forAll(pGen) {p ⇒ 
    val (a, b) = p
    val conc = a + "." + b
    adjustEnding(Seq(b))(a) ≟ conc 
  }
}

// vim: set ts=2 sw=2 et:
