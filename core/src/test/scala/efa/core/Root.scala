package efa.core

import Efa._
import org.scalacheck._, Prop._
import scalaz._, Scalaz._
import shapeless.{HList, HNil, ::, lens}
import shapeless.contrib.scalaz._

case class Root(id: Int, name: String, branches: List[Branch])

object Root {
  val l = lens[Root]
  val id = zlens(l >> 'id)
  val name = zlens(l >> 'name)
  val branches = zlens(l >> 'branches)

  implicit lazy val RootEqual = deriveEqual[Root]

  implicit lazy val RootUId = UniqueIdL lens id

  implicit lazy val RootArb = Arbitrary(
    for {
      n  ← Gen.identifier
      i  ← Gen choose (1, 10)
      ls ← Gen listOfN (i, Arbitrary.arbitrary[Branch])
    } yield Root(0, n, UniqueIdL[Branch,Int] generateIds ls)
  )

  implicit lazy val RootParent: ParentL[List,Root,Branch,Root :: HNil] =
    ParentL mplusRoot branches
}

// vim: set ts=2 sw=2 et:
