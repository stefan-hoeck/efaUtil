package efa.core

import scalaz._, Scalaz._

trait Lenses {
  def L[A]: A @> A = Lens.self

  def LP[A]: A @?> A = ~L[A]
}

object Lenses extends Lenses

// vim: set ts=2 sw=2 et:
