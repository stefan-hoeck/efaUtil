package efa.data

import scalaz._, Scalaz._

trait UniqueId[A,I] {
  def id (a: A): I

  def idMap (as: List[A]): Map[I,A] = pairs (as) toMap

  def pairs[F[_]:Functor] (as: F[A]): F[(I,A)] = as map (a ⇒ id (a) → a)

  def newId[F[_]] (as: F[A])(implicit m:Monoid[I], e:Enum[I], f:Foldable[F])
    : I = e succ f.foldLeft(as, m.zero)((i,a) ⇒ i max id(a))
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
