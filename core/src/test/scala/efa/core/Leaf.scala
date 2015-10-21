package efa.core

import Efa._
import org.scalacheck._, Prop._, Arbitrary.arbitrary
import scalaz.{Equal, @>, Lens, Scalaz}, Scalaz._, scalaz.scalacheck.ScalaCheckBinding._

case class Leaf(id: Int, name: String)

object Leaf {
  val id: Leaf @> Int = Lens.lensu((a,b) ⇒ a copy (id = b), _.id)

  implicit lazy val equalInst: Equal[Leaf] = Equal.equalA
  implicit lazy val uidInst: IntIdL[Leaf] = UniqueIdL lens id
  implicit lazy val arbInst: Arbitrary[Leaf] = Arbitrary(
    ^(Gen choose (0,100), Gen.identifier)(Leaf.apply)
  )
}

// vim: set ts=2 sw=2 et:
