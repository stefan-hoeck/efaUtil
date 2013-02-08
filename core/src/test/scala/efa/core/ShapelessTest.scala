package efa.core

import efa.core.Efa._
import org.scalacheck._, Prop._
import shapeless._, HList._, Nat._
import scalaz._, Scalaz._, scalaz.{Lens ⇒ Lensz}

object ShapelessTest extends Properties ("Shapeless") {
  val L = Lensz.self[Cc]
  val LO = ~L

  property("arbitrary_and_equal") = forAll {p: (Cc, Cc) ⇒ 
    val (a, b) = p

    (a ≟ b) == (a == b)
  }

  property("lenses") = forAll {c: Cc ⇒ 
    L.aString.get(c) ≟ c.aString
    L.anInt.get(c) ≟ c.anInt
    L.anOption.get(c) ≟ c.anOption
  }

  property("lensesO") = forAll {c: Cc ⇒ 
    LO.aString.get(c) ≟ c.aString.some
    LO.anInt.get(c) ≟ c.anInt.some
    LO.anOption.get(c) ≟ Some(c.anOption)
  }
}

case class Cc (aString: String, anInt: Int, anOption: Option[Int])

object Cc {
  implicit val CcIso = Iso.hlist(Cc.apply _, Cc.unapply _)
  implicit val CcEqual: Equal[Cc] = ccEqual
  implicit val CcArb: Arbitrary[Cc] = ccArbitrary

  val Lenses = SLens[Cc]

  implicit class CcLenses[A] (val l: A @> Cc) extends AnyVal {
    def aString = l >=> Lenses.at(_0)
    def anInt = l >=> Lenses.at(_1)
    def anOption = l >=> Lenses.at(_2)
  }

  implicit class CcLensesO[A] (val l: A @?> Cc) extends AnyVal {
    def aString = l >=> ~Lenses.at(_0)
    def anInt = l >=> ~Lenses.at(_1)
    def anOption = l >=> ~Lenses.at(_2)
  }
}

// vim: set ts=2 sw=2 et:
