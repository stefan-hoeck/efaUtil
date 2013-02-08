package efa.core

import org.scalacheck._, Prop._
import scalaz._, Scalaz._

case class Branch(id: Int, name: String, leaves: Map[Int,Leaf])

object Branch {
  implicit lazy val BranchEqual = Equal.equalA[Branch]

  implicit lazy val BranchUId = UniqueIdL lens id

  implicit lazy val BranchArb = Arbitrary(
    for {
      n  ← Gen.identifier
      ls ← Efa.mapGen[Leaf,Int](1, 10)
    } yield Branch(0, n, ls)
  )

  implicit lazy val BranchParent = Root.RootParent mapLensed leaves

  val id: Branch @> Int = Lens.lensu((a,b) ⇒ a.copy(id = b), _.id)

  val leaves: Branch @> Map[Int,Leaf] =
    Lens.lensu((a,b) ⇒ a.copy(leaves = b), _.leaves)
  
}

// vim: set ts=2 sw=2 et:
