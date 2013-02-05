package efa.core

import scalaz.{@>, Lens}

/**
  * Type class that allows the editing of a unique identifier
  * in an object of a given type.
  *
  * See UniqueId for a more thorough explanation about identifiers.
  */
trait UniqueIdL[A,I] extends UniqueId[A,I] {
  def idL: A @> I
  def id (a: A) = idL get a
}

object UniqueIdL extends UniqueIdLFunctions {
  @inline def apply[A,I](implicit U: UniqueIdL[A,I]): UniqueIdL[A,I] = U
}

trait UniqueIdLFunctions {

  def intIdL[A] (l: A @> Int): IntIdL[A] = lens (l)

  def longIdL[A] (l: A @> Long): LongIdL[A] = lens (l)

  def stringIdL[A] (l: A @> String): StringIdL[A] = lens (l)

  def idL[A]: UniqueIdL[A, A] = lens (Lens.self)

  def lens[A,I] (l: A @> I): UniqueIdL[A,I] = new UniqueIdL[A,I] {
    val idL = l
  }
}

// vim: set ts=2 sw=2 et:
