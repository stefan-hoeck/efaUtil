package efa.data

import scalaz.Show

trait Named[A] extends Show[A] {
  def name (a: A): String

  def nameSort (as: List[A]): List[A] = as sortBy name

  override def shows (a: A): String = name (a)
}

object Named {
  @inline def apply[A:Named]: Named[A] = implicitly
}

// vim: set ts=2 sw=2 et:
