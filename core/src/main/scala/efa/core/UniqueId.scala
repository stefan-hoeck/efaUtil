package efa.core

trait UniqueId[-A,+I] {
  def id (a: A): I
}

object UniqueId {
  @inline def apply[A,I](implicit U: UniqueId[A,I]): UniqueId[A,I] = U

  def get[A, B] (f: A ⇒ B): UniqueId[A, B] = new UniqueId[A, B] {
    def id (a: A) = f(a)
  }

  def unique[A]: UniqueId[A, A] = get (identity)

  def trivial[A]: UniqueId[A,Unit] = get (_ ⇒ ())
}

// vim: set ts=2 sw=2 et:
