package efa.data

import scalaz._, Scalaz._
import scalaz.std.indexedSeq._

trait Named[A] extends Show[A] {
  def name (a: A): String

  final def nameSort (as: List[A]): List[A] = as sortBy name

  final def nameSort (as: IxSq[A]): IxSq[A] = as sortBy name

  final def sortedPairs[B] (m: Map[B,A]): List[(B,A)] =
    m.toList sortBy (p ⇒ name (p._2))

  override def shows (a: A): String = name (a)
}

object Named {
  @inline def apply[A:Named]: Named[A] = implicitly
}

// vim: set ts=2 sw=2 et:
