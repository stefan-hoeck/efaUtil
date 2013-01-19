package efa.data

import scalaz.@>

trait UniqueIdL[A,I] extends UniqueId[A,I] {
  def idL: A @> I
  def id (a: A) = idL get a
}

object UniqueIdL {
  @inline def apply[A,I](implicit U: UniqueIdL[A,I]): UniqueIdL[A,I] = U

  def lens[A,I] (l: A @> I): UniqueIdL[A,I] = new UniqueIdL[A,I] {
    val idL = l
  }
}

// vim: set ts=2 sw=2 et:
