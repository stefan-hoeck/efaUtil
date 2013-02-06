package efa.core

import scalaz._, Scalaz._

/**
  * Some shortcut function for working with Lenses
  */
trait Lenses {
  def L[A]: A @> A = Lens.self

  def LP[A]: A @?> A = ~L[A]

  def foldableLookupBy[F[_]:Foldable:Functor,A](p: A ⇒ Boolean): F[A] @?> A = 
    PLens.plens { f ⇒ 
      f.toList find p map { a ⇒ Store(n ⇒ f map { o ⇒ p(o) ? n | o }, a) }
    }

  def foldableLookup[F[_]:Foldable:Functor,A:Equal](a: A): F[A] @?> A = 
    foldableLookupBy[F,A](a ≟ _)
}

object Lenses extends Lenses

// vim: set ts=2 sw=2 et:
