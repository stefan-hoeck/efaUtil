package efa.core.std

import scalaz._, Scalaz._

trait MapInstances {
  implicit def mapEqual[K: Order, V: Equal]: Equal[Map[K, V]] = new Equal[Map[K, V]] {
    def equal(a1: Map[K, V], a2: Map[K, V]): Boolean = {
      if (equalIsNatural) a1 == a2
      else Equal[Set[K]].equal(a1.keySet, a1.keySet) && {
        a1.forall {
          case (k, v) => Equal[V].equal(v, a2(k))
        }
      }
    }
    override val equalIsNatural: Boolean = Equal[K].equalIsNatural && Equal[V].equalIsNatural
  }
}

// vim: set ts=2 sw=2 et:
