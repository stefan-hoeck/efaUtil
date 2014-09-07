package efa.core

import Efa._
import efa.core.syntax.lens
import org.scalacheck._, Prop._
import scalaz._, Scalaz._

case class Leaf(id: Id, name: Name)

object Leaf {
  val l = L[Leaf]
  val id: Leaf @> Id = l >> 'id
  implicit lazy val equalInst: Equal[Leaf] = typeclass.equal
  implicit lazy val uidInst: UIdL[Leaf] = UniqueIdL lens id
  implicit lazy val arbInst: Arbitrary[Leaf] = typeclass.arbitrary
}

// vim: set ts=2 sw=2 et:
