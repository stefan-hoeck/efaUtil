package efa.core

import scalaz._, Scalaz._

/** Some shortcut function for working with Lenses
  *
  * See [[scalaz.Lens]] and [[scalaz.PLens]] for more information
  * on `Lens`es.
  */
trait Lenses {

  /** Provides a shortcut to the self-Lens
    */
  def L[A]: A @> A = Lens.self

  /** Provides a shortcut to the self partial Lens
    */
  def LP[A]: A @?> A = ~L[A]

  /** Partial lens that looks up an item in a [[scalaz.Foldable]]
    * data structure using the given predicate.
    */
  def foldableLookupBy[F[_]:Foldable:Functor,A](p: A ⇒ Boolean): F[A] @?> A = 
    PLens.plens { f ⇒ 
      f.toList find p map { a ⇒ Store(n ⇒ f map { o ⇒ p(o) ? n | o }, a) }
    }

  /** Partial lens that looks up an item in a [[scalaz.Foldable]]
    * data structure using [[scalaz.Equal]]
    */
  def foldableLookup[F[_]:Foldable:Functor,A:Equal](a: A): F[A] @?> A = 
    foldableLookupBy { a ≟ _ }
}

object Lenses extends Lenses

// vim: set ts=2 sw=2 et:
