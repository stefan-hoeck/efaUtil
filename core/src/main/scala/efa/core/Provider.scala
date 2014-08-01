package efa.core

import scalaz.{DList, Functor}

/** A type class that gives access to a list of objects of a given type.
  *
  * This type class has its uses as a service provider
  */
trait Provider[A] { self ⇒ 
  def get: DList[A]
}

object Provider {
  def apply[A:Provider]: Provider[A] = implicitly

  def map[A,B](f: A ⇒ B)(implicit A:Provider[A]): Provider[B] =
    new Provider[B] {
      def get = A.get map f
    }

  implicit val ProviderFunctor: Functor[Provider] = new Functor[Provider] {
    def map[A,B](p: Provider[A])(f: A ⇒ B) = Provider.map(f)(p)
  }
}

// vim: set ts=2 sw=2 et:
