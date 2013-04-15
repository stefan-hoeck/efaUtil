package efa.core.syntax

import org.openide.util.Lookup
import scalaz.effect.IO
import efa.core.std.{lookup ⇒ l}

trait ToLookupOps {
  implicit class LookupOps(val self: Lookup) {
    def head[A:Manifest]: IO[Option[A]] = l head self
    def all[A:Manifest]: IO[List[A]] = l all self
  }
}

// vim: set ts=2 sw=2 et:
