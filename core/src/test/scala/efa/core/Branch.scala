package efa.core

//import Efa._
//import shapeless._
//import org.scalacheck._, Prop._
//import scalaz._, Scalaz._
//import shapeless.contrib.scalaz._
//
//case class Branch(id: Id, name: Name, leaves: Map[Id,Leaf])
//
//object Branch {
//  val l = lens[Branch]
//  val id = l >> 'id
//  val leaves = l >> 'leaves
//
//  implicit lazy val uidInst: UIdL[Branch] = UniqueIdL lens (l >> 'id toScalaz)
//  implicit lazy val mapArb: Arbitrary[Map[Id,Leaf]] = mapArbitrary[Leaf,Id](1, 10)
//  implicit lazy val equalInst: Equal[Branch] = implicitly[Equal[Branch]]
//  implicit lazy val arbInst: Arbitrary[Branch] = implicitly[Arbitrary[Branch]]
//  implicit lazy val BranchParent = Root.RootParent mapLensed leaves.toScalaz
//}

// vim: set ts=2 sw=2 et:
