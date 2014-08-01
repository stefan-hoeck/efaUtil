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
  def L[A]: A @> A = Lens.lensId

  /** Provides a shortcut to the self partial Lens
    */
  def LP[A]: A @?> A = ~L[A]

  /** Partial lens that looks up an item in a `scalaz.Traverse`
    * data structure using the given predicate.
    */
  def traverseLookupBy[F[_]:Traverse,A](p: A ⇒ Boolean): F[A] @?> A = 
    PLens.plens { f ⇒ 
      f.toList find p map { a ⇒ Store(n ⇒ f map { o ⇒ p(o) ? n | o }, a) }
    }

  /** Partial lens that looks up an item in a `scalaz.Traverse`
    * data structure using `scalaz.Equal`
    */
  def traverseLookup[F[_]:Traverse,A:Equal](a: A): F[A] @?> A = 
    traverseLookupBy { a ≟ _ }

  def zlens[A,B](l: shapeless.Lens[A,B]): A @> B =
    scalaz.LensFamily.lensg(l.set, l.get)

  def slens[A,B](l: A @> B): shapeless.Lens[A, B] =
    new shapeless.Lens[A, B] {
      def get(a: A): B = l.get(a)
      def set(a: A)(b: B): A = l.set(a, b)
    }
}

object Lenses extends Lenses

// vim: set ts=2 sw=2 et:
