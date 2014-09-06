package efa.core

import efa.core.std.anyVal._
import org.scalacheck.{Arbitrary ⇒ Arb, Gen}
import scalaz.{Show, Enum, Monoid}
import scalaz.std.anyVal._

final case class Id(v: Long) extends AnyVal

object Id extends Function1[Long,Id] {
  implicit val showInst: Show[Id] = show contramap (_.v)
  implicit val enumInst: Enum[Id] = enum.xmap(Id)(_.v)
  implicit val monoidInst: Monoid[Id] = monoid.xmap(Id)(_.v)
  implicit val defaultInst: Default[Id] = Default default Id(1L)
  implicit val readInst: Read[Id] = Read map Id
  implicit val arbInst: Arb[Id] = Arb(Gen choose (-1L, 1000L) map Id)
}

// vim: set ts=2 sw=2 et:
