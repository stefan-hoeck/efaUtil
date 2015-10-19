package efa.core
//
//import Efa._
//import org.scalacheck._, Prop._
//import scalaz._, Scalaz._
//import shapeless._
//import shapeless.contrib.scalaz._
//
//case class Leaf(id: Id, name: Name)
//
//object Leaf {
//  val l = lens[Leaf]
//  val id: Leaf @> Id = (l >> 'id).toScalaz
//  implicit lazy val equalInst: Equal[Leaf] = deriveEqual
//  implicit lazy val uidInst: UIdL[Leaf] = UniqueIdL lens id
//  implicit lazy val arbInst: Arbitrary[Leaf] = implicitly
//}
//
//// vim: set ts=2 sw=2 et:
