package efa.core

import Efa._
import org.scalacheck._, Prop._, Arbitrary.arbitrary
import scalaz.{Equal, @>, Lens, Scalaz}, Scalaz._, scalaz.scalacheck.ScalaCheckBinding._

case class Branch(id: Int, name: String, leaves: Map[Int,Leaf])

object Branch {
  val id: Branch @> Int = Lens.lensu((a,b) ⇒ a copy (id = b), _.id)
  val name: Branch @> String = Lens.lensu((a,b) ⇒ a copy (name = b), _.name)
  val leaves: Branch @> Map[Int,Leaf] = Lens.lensu((a,b) ⇒ a copy (leaves = b), _.leaves)

  implicit lazy val uidInst: IntIdL[Branch] = UniqueIdL lens id
  implicit lazy val mapArb: Arbitrary[Map[Int,Leaf]] = mapArbitrary[Leaf,Int](1, 10)
  implicit lazy val equalInst: Equal[Branch] = Equal.equalA
  implicit lazy val arbInst: Arbitrary[Branch] = Arbitrary(
    ^^(Gen choose (0,1000), Gen.identifier, arbitrary[Map[Int,Leaf]])(Branch.apply)
  )
  implicit lazy val BranchParent = Root.RootParent mapLensed leaves
}

// vim: set ts=2 sw=2 et:
