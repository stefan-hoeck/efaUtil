package efa.core

import shapeless._, HList._
import org.scalacheck.{Gen, Arbitrary}, Arbitrary.{arbitrary ⇒ arb}
import scalaz._, Scalaz._

trait ShapelessInstances {

  def ccDefault[C, L <: HList](implicit iso: Iso[C, L], md: Default[L])
  : Default[C] = Default default iso.from (md.default)

  implicit val hnilDefault: Default[HNil] = Default default HNil
  
  implicit def hlistDefault[H:Default, T <: HList:Default]
  : Default[H :: T] = Default default (Default.!!![H] :: Default.!!![T])

  def ccMonoid[C, L <: HList](implicit iso: Iso[C, L], ml: Monoid[L]) =
    new Monoid[C] {
      val zero = iso from ml.zero
      def append(a : C, b : ⇒ C) = iso from (ml append (iso to a, iso to b))
    }

  implicit val hnilMonoid : Monoid[HNil] = new Monoid[HNil] {
    val zero = HNil
    def append(a : HNil, b : ⇒ HNil) = HNil
  }
  
  implicit def hlistMonoid[H, T <: HList](implicit mh: Monoid[H], mt: Monoid[T])
  : Monoid[H :: T] = new Monoid[H :: T] {
    val zero = mh.zero :: mt.zero
    def append(a : H :: T, b : ⇒ H :: T) = 
      mh.append (a.head, b.head) :: mt.append (a.tail, b.tail)  
  }

  def ccArbitrary[C, L <: HList](implicit iso: Iso[C, L], ml: Arbitrary[L])
  : Arbitrary[C] = Arbitrary (arb[L] map iso.from)

  implicit val hnilArbitrary : Arbitrary[HNil] = Arbitrary (HNil)
  
  implicit def hlistArbitrary[H:Arbitrary, T <: HList:Arbitrary]
  : Arbitrary[H :: T] = Arbitrary (
    for { h ← arb[H]; t ← arb[T] } yield h :: t
  )

  def ccEqual[C, L <: HList](implicit iso: Iso[C, L], el: Equal[L])
  : Equal[C] = Equal equalBy iso.to

  implicit val hnilEqual : Equal[HNil] = Equal equal ((_,_) ⇒ true)
  
  implicit def hlistEqual[H:Equal, T <: HList:Equal]
  : Equal[H :: T] = Equal equal ((a,b) ⇒ 
    Equal[H].equal (a.head, b.head) && Equal[T].equal (a.tail, b.tail)
  )
}

object Shapeless extends ShapelessInstances

// vim: set ts=2 sw=2 et:
