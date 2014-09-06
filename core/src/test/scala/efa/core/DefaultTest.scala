package efa.core

import Default.!!!
import org.scalacheck.Properties
import Shapeless._
import scalaz.Equal
import scalaz.syntax.equal._

case class DefaultCc(id: Id, name: Name)

object DefaultCc {
  implicit val equalInst: Equal[DefaultCc] = deriveEqual
  implicit val defaultInst: Default[DefaultCc] = Default.derive
}

object DefaultTest extends Properties("Default") {
  property("derive") = !!![DefaultCc] ≟ DefaultCc(!!![Id], !!![Name])
}

// vim: set ts=2 sw=2 et:
