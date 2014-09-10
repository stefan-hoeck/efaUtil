package efa.core

import scala.language.experimental.macros
import scalaz.{Monoid, Functor}
import shapeless.{ProductTypeClass, HNil, HList, ::, GenericMacros}

/** A type clars that represents a default value for a given type.
  */
trait Default[A] {
  val default: A
}

trait DefaultFunctions {
  def default[A](a: A): Default[A] = new Default[A]{val default = a}

  def !!![A:Default]:A = Default[A].default

  def monoid[A:Monoid]: Default[A] = default(Monoid[A].zero)
}

object Default extends DefaultFunctions {
  def apply[A:Default]: Default[A] = implicitly
  
  def map[A,B](f: A ⇒ B)(implicit A: Default[A]) = default(f(A.default))

  implicit val productTCInst: ProductTypeClass[Default] = new ProductTypeClass[Default] {
    val emptyProduct: Default[HNil] = default(HNil)
    def project[F,G](inst: ⇒ Default[G], to: F ⇒ G, from: G ⇒ F) = map(from)(inst)
    def product[H,T <: HList](ch: Default[H], ct: Default[T]): Default[H :: T] =
      default(ch.default :: ct.default)
  }

  def derive[A](implicit ev: ProductTypeClass[Default]): Default[A] =
    macro GenericMacros.deriveProductInstance[Default, A]
}

// vim: set ts=2 sw=2 et:
