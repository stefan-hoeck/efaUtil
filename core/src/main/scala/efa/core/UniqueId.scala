package efa.core

import scalaz._, Scalaz._

/** Type class that links objects of a given type to unique
  * identifiers of another type.
  *
  * That identifiers are indeed unique is the client's responsibility. Also,
  * what 'unique' identifier means, is not clearly defined. Typically it is
  * assumed, that in a collection of objects, each identifier is indeed unique.
  * This makes transforming such a collection to a Map with identifiers as
  * keys, trivial.
  *
  * Well behaved identifiers are suitable to be used as keys in Maps, provide
  * an implementation of type class Equal (Enum would be even better), and
  * are - of course - immutable.
  */
trait UniqueId[A,I] { self ⇒
  def id (a: A): I

  def idPair (a: A): (I, A) = id(a) → a

  def idMap[F[_]:Foldable](as: F[A]): Map[I,A] = pairs(as.toList) toMap

  def pairs[F[_]:Functor](as: F[A]): F[(I,A)] = as map idPair

  /** Creates a unique identifier from a collection of values.
    *
    * If the collection is empty, the identifier is Monoid zero, 
    * otherwise the maximum identifier is determined and its
    * successor returned.
    */
  def newId[F[_]:Foldable]
    (as: F[A])(implicit m: Monoid[I], e: Enum[I]): I =
    e succ as.foldLeft(m.zero) { (i,a) ⇒ i max id(a) }

  def contramap[B](f: B ⇒ A): UniqueId[B,I] = new UniqueId[B,I] {
    def id(b: B) = self id f(b)
  }

  def ∙ [B](f: B ⇒ A): UniqueId[B,I] = contramap(f)
}

object UniqueId extends UniqueIdFunctions {
  @inline def apply[A,I](implicit U: UniqueId[A,I]): UniqueId[A,I] = U
}

trait UniqueIdFunctions {
  def get[A, B] (f: A ⇒ B): UniqueId[A, B] = new UniqueId[A, B] {
    def id (a: A) = f(a)
  }

  def intId[A] (f: A ⇒ Int): IntId[A] = get (f)

  def longId[A] (f: A ⇒ Long): LongId[A] = get (f)

  def id[A]: UniqueId[A, A] = get (identity)

  def trivial[A]: UniqueId[A,Unit] = get (_ ⇒ ())
}

// vim: set ts=2 sw=2 et:
