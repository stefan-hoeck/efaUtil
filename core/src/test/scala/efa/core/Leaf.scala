package efa.core

import Efa._
import org.scalacheck._, Prop._
import scalaz._, Scalaz._
import shapeless.lens

case class Leaf(id: Int, name: String)

object Leaf {
  lazy val id = lens[Leaf] >> 'id asZ

  implicit lazy val LeafEqual = deriveEqual[Leaf]

  implicit lazy val LeafUId = UniqueIdL lens id

  implicit lazy val LeafArb = Arbitrary(Gen.identifier map (Leaf(0,_)))
}

// vim: set ts=2 sw=2 et:
