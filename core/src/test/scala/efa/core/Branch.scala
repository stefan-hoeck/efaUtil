package efa.core

import Efa._
import org.scalacheck._, Prop._, Arbitrary.arbitrary
import scalaz.{Equal, @>, Lens, Scalaz}, Scalaz._, scalaz.scalacheck.ScalaCheckBinding._

case class Branch(id: Id, name: Name, leaves: Map[Id,Leaf])

object Branch {
  val id: Branch @> Id = Lens.lensu((a,b) ⇒ a copy (id = b), _.id)
  val name: Branch @> Name = Lens.lensu((a,b) ⇒ a copy (name = b), _.name)
  val leaves: Branch @> Map[Id,Leaf] = Lens.lensu((a,b) ⇒ a copy (leaves = b), _.leaves)

  implicit lazy val uidInst: UIdL[Branch] = UniqueIdL lens id
  implicit lazy val mapArb: Arbitrary[Map[Id,Leaf]] = mapArbitrary[Leaf,Id](1, 10)
  implicit lazy val equalInst: Equal[Branch] = Equal.equalA
  implicit lazy val arbInst: Arbitrary[Branch] = Arbitrary(
    ^^(arbitrary[Id], arbitrary[Name], arbitrary[Map[Id,Leaf]])(Branch.apply)
  )
  implicit lazy val BranchParent = Root.RootParent mapLensed leaves
}

// vim: set ts=2 sw=2 et:
