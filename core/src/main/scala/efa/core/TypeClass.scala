package efa.core

import org.scalacheck.{Shrink, Arbitrary}
import scala.language.experimental.macros
import scalaz.{Semigroup, Monoid, Equal, Show, Order}
import shapeless.{ProductTypeClass, TypeClass, GenericMacros}
import shapeless.contrib.scalaz.instances
import shapeless.contrib.scalacheck

trait TypeClassInstances {
  implicit val ArbitraryI: TypeClass[Arbitrary] = scalacheck.ArbitraryI
  implicit val ShrinkI: TypeClass[Shrink] = scalacheck.ShrinkI
  implicit val SemigroupI: ProductTypeClass[Semigroup] = instances.SemigroupI
  implicit val MonoidI: ProductTypeClass[Monoid] = instances.MonoidI
  implicit val EqualI: TypeClass[Equal] = instances.EqualI
  implicit val ShowI: TypeClass[Show] = instances.ShowI
  implicit val OrderI: TypeClass[Order] = instances.OrderI
}

trait TypeClassFunctions {
  def arbitrary[T](implicit ev: TypeClass[Arbitrary]): Arbitrary[T] =
    macro GenericMacros.deriveInstance[Arbitrary, T]

  def shrink[T](implicit ev: TypeClass[Shrink]): Shrink[T] =
    macro GenericMacros.deriveInstance[Shrink, T]

  def semigroup[T](implicit ev: ProductTypeClass[Semigroup]): Semigroup[T] =
    macro GenericMacros.deriveProductInstance[Semigroup, T]

  def monoid[T](implicit ev: ProductTypeClass[Monoid]): Monoid[T] =
    macro GenericMacros.deriveProductInstance[Monoid, T]

  def equal[T](implicit ev: TypeClass[Equal]): Equal[T] =
    macro GenericMacros.deriveInstance[Equal, T]

  def order[T](implicit ev: TypeClass[Order]): Order[T] =
    macro GenericMacros.deriveInstance[Order, T]

  def show[T](implicit ev: TypeClass[Show]): Show[T] =
    macro GenericMacros.deriveInstance[Show, T]
}

object typeclass extends TypeClassInstances with TypeClassFunctions

// vim: set ts=2 sw=2 et:
