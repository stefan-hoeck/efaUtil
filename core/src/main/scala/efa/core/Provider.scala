package efa.core

import scalaz.DList

trait Provider[A] {
  def get: DList[A]
}

// vim: set ts=2 sw=2 et:
