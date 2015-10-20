package efa.core

import Efa._
import org.scalacheck._, Prop._, Arbitrary.arbitrary
import scalaz.{Equal, @>, Lens, Scalaz}, Scalaz._, scalaz.scalacheck.ScalaCheckBinding._

case class Leaf(id: Id, name: Name)

object Leaf {
  val id: Leaf @> Id = Lens.lensu((a,b) ⇒ a copy (id = b), _.id)

  implicit lazy val equalInst: Equal[Leaf] = Equal.equalA
  implicit lazy val uidInst: UIdL[Leaf] = UniqueIdL lens id
  implicit lazy val arbInst: Arbitrary[Leaf] = Arbitrary(
    ^(arbitrary[Id], arbitrary[Name])(Leaf.apply)
  )
}

// vim: set ts=2 sw=2 et:
