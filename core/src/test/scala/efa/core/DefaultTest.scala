package efa.core

import Default.!!!
import org.scalacheck.Properties
import scalaz.Equal
import scalaz.syntax.equal._
import shapeless._
import efa.core.typeclass._

case class DefaultCc(id: Id, name: Name)

object DefaultCc {
  implicit val equalInst: Equal[DefaultCc] = efa.core.typeclass.equal
  implicit val defaultInst: Default[DefaultCc] = efa.core.Default.deriveInstance
}

object DefaultTest extends Properties("Default") {
  property("derive") = !!![DefaultCc] ≟ DefaultCc(!!![Id], !!![Name])
}

// vim: set ts=2 sw=2 et:
