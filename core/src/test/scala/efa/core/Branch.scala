package efa.core

import Efa._
import org.scalacheck._, Prop._
import scalaz._, Scalaz._
import shapeless.lens

case class Branch(id: Int, name: String, leaves: Map[Int,Leaf])

object Branch {
  lazy val l = lens[Branch]
  lazy val id = zlens(l >> 'id)
  lazy val leaves = zlens(l >> 'leaves)

  implicit lazy val BranchEqual = deriveEqual[Branch]

  implicit lazy val BranchUId = UniqueIdL lens id

  implicit lazy val BranchArb = Arbitrary(
    for {
      n  ← Gen.identifier
      ls ← Efa.mapGen[Leaf,Int](1, 10)
    } yield Branch(0, n, ls)
  )

  implicit lazy val BranchParent = Root.RootParent mapLensed leaves
}

// vim: set ts=2 sw=2 et:
