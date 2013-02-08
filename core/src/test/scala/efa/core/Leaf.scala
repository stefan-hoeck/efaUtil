package efa.core

import org.scalacheck._, Prop._
import scalaz._, Scalaz._

case class Leaf(id: Int, name: String)

object Leaf {
  implicit lazy val LeafEqual = Equal.equalA[Leaf]

  implicit lazy val LeafUId = UniqueIdL lens id

  implicit lazy val LeafArb = Arbitrary(
    Gen.identifier map { Leaf(0, _) }
  )

  val id: Leaf @> Int = Lens.lensu((a,b) ⇒ a.copy(id = b), _.id)
}

// vim: set ts=2 sw=2 et:
