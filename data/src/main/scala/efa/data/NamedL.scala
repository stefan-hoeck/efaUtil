package efa.data

import scalaz.@>

trait NamedL[A] extends Named[A] {
  def nameL: A @> String
  override def name (a: A): String = nameL get a
}

object NamedL {
  def apply[A:NamedL]: NamedL[A] = implicitly
}

// vim: set ts=2 sw=2 et:
