package efa.core

import scalaz.Monoid, scalaz.syntax.monoid._

/** A type class that represents a default value for a given type.
  */
trait Default[+A] {
  val default: A

  def map[B](f: A ⇒ B): Default[B] = Default.default(f(default))

  def ∘ [B](f: A ⇒ B): Default[B] = map(f)
}

trait DefaultFunctions {
  def default[A](a: A): Default[A] = new Default[A]{val default = a}

  def !!![A:Default]:A = Default[A].default

  def monoid[A:Monoid]: Default[A] = default(∅)
}

trait DefaultInstances {
}

object Default extends DefaultFunctions with DefaultInstances {
  def apply[A:Default]: Default[A] = implicitly
}

// vim: set ts=2 sw=2 et:
