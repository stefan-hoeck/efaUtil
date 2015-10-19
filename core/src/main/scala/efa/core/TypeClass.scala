package efa.core

import org.scalacheck.{Shrink, Arbitrary}
import scalaz.{Semigroup, Monoid, Equal, Show, Order}

trait TypeClassInstances {
}

trait TypeClassFunctions extends shapeless.contrib.scalaz.Instances {
//  def arbitrary[T,G](implicit gen: Generic.Aux[T, G], cg: Lazy[Arbitrary[G]]): Arbitrary[T] =
//    ArbitraryDerivedOrphans.deriveInstance
//
//  def semigroup[T,G](implicit gen: Generic.Aux[T, G], cg: Lazy[Semigroup[G]]): Semigroup[T] =
//    SemigroupDerivedOrphans.deriveInstance
//
//  def monoid[T,G](implicit gen: Generic.Aux[T, G], cg: Lazy[Monoid[G]]): Monoid[T] =
//    MonoidDerivedOrphans.deriveInstance
//
//  def equal[T,G](implicit gen: Generic.Aux[T, G], cg: Lazy[Equal[G]]): Equal[T] =
//    EqualDerivedOrphans.deriveInstance
//
//  def order[T,G](implicit gen: Generic.Aux[T, G], cg: Lazy[Order[G]]): Order[T] =
//    OrderDerivedOrphans.deriveInstance
//
//  def show[T,G](implicit gen: Generic.Aux[T, G], cg: Lazy[Show[G]]): Show[T] =
//    ShowDerivedOrphans.deriveInstance
}

object typeclass extends TypeClassInstances with TypeClassFunctions {
}

// vim: set ts=2 sw=2 et:
