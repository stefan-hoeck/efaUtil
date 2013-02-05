package efa.core.std

import scalaz.{StateT, State, Scalaz}, Scalaz.Id

trait StateFunctions {
  def toState[S,A](f: S ⇒ (S,A)): State[S,A] = StateT[Id,S,A](f)
}

object state extends StateFunctions

// vim: set ts=2 sw=2 et:
