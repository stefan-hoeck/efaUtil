package efa.core

import scalaz.DList

/** A type class that gives access to a list of objects of a given type.
  *
  * This type class has its uses as a service provider
  */
trait Provider[A] { self ⇒ 
  def get: DList[A]

  def map[B](f: A ⇒ B): Provider[B] = new Provider[B] {
    def get = self.get map f
  }

  def ∘ [B](f: A ⇒ B): Provider[B] = map(f)
}

object Provider {
  def apply[A:Provider]: Provider[A] = implicitly
}

// vim: set ts=2 sw=2 et:
