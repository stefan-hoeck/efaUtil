package efa.core

import scalaz.@>

/** Type class that associates an editable (and typically localized)
  * name with objects of a type.
  *
  * The name field of an object can be updated via lens nameL.
  *
  * @see [[scalaz.Lens]]
  */
trait NamedL[A] extends Named[A] {
  def nameL: A @> String

  final override def name (a: A): String = nameL get a
}

object NamedL {
  def apply[A:NamedL]: NamedL[A] = implicitly
}

// vim: set ts=2 sw=2 et:
