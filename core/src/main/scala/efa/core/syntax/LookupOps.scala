package efa.core.syntax

import org.openide.util.Lookup
import scalaz.syntax.Ops
import scalaz.effect.IO
import efa.core.std.{lookup ⇒ l}

trait LookupOps extends Ops[Lookup] {

  def head[A:Manifest]: IO[Option[A]] = l head self

  def all[A:Manifest]: IO[List[A]] = l all self
}

trait ToLookupOps {
  implicit def ToLookupOps(a: Lookup): LookupOps =
    new LookupOps{def self = a}
}

// vim: set ts=2 sw=2 et:
