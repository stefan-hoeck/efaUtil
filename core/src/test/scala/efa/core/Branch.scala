package efa.core

import Efa._
import efa.core.syntax.lens
import org.scalacheck._, Prop._
import scalaz._, Scalaz._

case class Branch(id: Id, name: Name, leaves: Map[Id,Leaf])

object Branch {
  val l = L[Branch]
  val id = l >> 'id
  val leaves = l >> 'leaves

  implicit lazy val uidInst: UIdL[Branch] = UniqueIdL lens (l >> 'id)
  implicit lazy val mapArb: Arbitrary[Map[Id,Leaf]] = mapArbitrary[Leaf,Id](1, 10)
  implicit lazy val equalInst: Equal[Branch] = typeclass.equal
  implicit lazy val arbInst: Arbitrary[Branch] = typeclass.arbitrary
  implicit lazy val BranchParent = Root.RootParent mapLensed leaves
}

// vim: set ts=2 sw=2 et:
