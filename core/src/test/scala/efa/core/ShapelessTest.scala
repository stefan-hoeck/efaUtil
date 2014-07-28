package efa.core

import efa.core.Efa._
import org.scalacheck._, Prop._
import shapeless._, HList._
import scalaz._, Scalaz._, scalaz.{Lens ⇒ Lensz}

object ShapelessTest extends Properties ("Shapeless") {
  val L = Lensz.lensId[Cc]
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
  implicit val equal: Equal[Cc] = deriveEqual
  implicit val arbitrary: Arbitrary[Cc] = deriveArbitrary

  val Lenses = lens[Cc]
  val aString = (Lenses >> 'aString).asZ
  val anInt = (Lenses >> 'anInt).asZ
  val anOption = (Lenses >> 'anOption).asZ

  implicit class CcLenses[A](val l: A @> Cc) extends AnyVal {
    def aString = l >=> Cc.aString
    def anInt = l >=> Cc.anInt
    def anOption = l >=> Cc.anOption
  }

  implicit class CcLensesO[A](val l: A @?> Cc) extends AnyVal {
    def aString = l >=> ~Cc.aString
    def anInt = l >=> ~Cc.anInt
    def anOption = l >=> ~Cc.anOption
  }
}

// vim: set ts=2 sw=2 et:
