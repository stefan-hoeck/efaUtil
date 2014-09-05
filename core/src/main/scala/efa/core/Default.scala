package efa.core

import scalaz.{Monoid, Functor}

/** A type class that represents a default value for a given type.
  */
trait Default[+A] {
  val default: A
}

trait DefaultFunctions {
  def default[A](a: A): Default[A] = new Default[A]{val default = a}

  def !!![A:Default]:A = Default[A].default

  def monoid[A:Monoid]: Default[A] = default(Monoid[A].zero)
}

object Default extends DefaultFunctions {
  def apply[A:Default]: Default[A] = implicitly

  def map[A,B](f: A ⇒ B)(implicit A: Default[A]) = default(f(A.default))
}

// vim: set ts=2 sw=2 et:
