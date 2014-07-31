package efa.core

import scalaz._, Scalaz._
import scalaz.std.indexedSeq._

/** Type class that associates a (typically localized) name with
  * an object of a type.
  *
  * Implementations of this type class can not only be used to display
  * names of objects in some output device, but also to sort collections
  * of objects by name.
  */
trait Named[A] extends Show[A] { self ⇒
  def name (a: A): String

  final def nameSort(as: List[A]): List[A] = as sortBy name

  final def nameSort(as: IxSq[A]): IxSq[A] = as sortBy name

  final def nameSortF[F[_]:Foldable](as: F[A]): List[A] = nameSort(as.toList)

  final def sortedPairs[B](m: Map[B,A]): List[(B,A)] =
    m.toList sortBy { p ⇒ name (p._2) }

  override def shows(a: A): String = name (a)

  def contramap[B](f: B ⇒ A): Named[B] = new Named[B] {
    def name(b: B) = self name f(b)
  }

  def ∙ [B](f: B ⇒ A): Named[B] = contramap(f)
}

object Named {
  @inline def apply[A:Named]: Named[A] = implicitly
}

// vim: set ts=2 sw=2 et:
