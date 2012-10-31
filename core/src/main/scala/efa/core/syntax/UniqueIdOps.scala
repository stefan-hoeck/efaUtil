package efa.core.syntax

import efa.core.UniqueId
import scalaz.syntax.Ops

trait UniqueIdOps[A,I] extends Ops[A] {
  implicit def F: UniqueId[A,I]

  def id: I = F id self
}

trait ToUniqueIdOps {
  implicit def ToUniqueIdOps[A,I](a: A)(implicit U: UniqueId[A,I])
  : UniqueIdOps[A,I] =
    new UniqueIdOps[A,I]{def self = a; def F = U}
}

// vim: set ts=2 sw=2 et:
