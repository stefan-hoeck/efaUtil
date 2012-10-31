package efa.core.std

import scalaz._, Scalaz._

trait StateFunctions {
  def toState[S,A](f: S ⇒ (S,A)): State[S,A] = StateT[Id,S,A](f)
}

object state extends StateFunctions

// vim: set ts=2 sw=2 et:
