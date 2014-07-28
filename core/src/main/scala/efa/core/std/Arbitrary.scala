package efa.core.std

import scala.language.experimental.macros
import shapeless._
import shapeless.contrib._
import org.scalacheck.{Arbitrary, Gen}

trait ArbitraryInstances {
  implicit def ArbitraryI: ProductTypeClass[Arbitrary] =
    new ProductTypeClass[Arbitrary] {
      def emptyProduct = Arbitrary(Gen value HNil)
      def product[F, T <: HList](f: Arbitrary[F], t: Arbitrary[T]) = Arbitrary(
        for { a ← f.arbitrary; b ← t.arbitrary } yield ::(a,b)
      )
      def project[A, B](b: => Arbitrary[B], ab: A => B, ba: B => A) =
        Arbitrary(b.arbitrary map ba)
    }

  implicit def deriveArbitrary[T](implicit ev: ProductTypeClass[Arbitrary])
    : Arbitrary[T] =
    macro GenericMacros.deriveProductInstance[Arbitrary, T]
}

// vim: set ts=2 sw=2 et:
