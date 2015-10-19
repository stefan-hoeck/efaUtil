package efa.core
//
//import Efa._
//import efa.core.syntax.lens
//import org.scalacheck._, Prop._
//import scalaz._, Scalaz._
//import shapeless.{HList, HNil, ::}
//
//case class Root(id: Id, name: Name, branches: List[Branch])
//
//object Root {
//  val l = L[Root]
//  val id = l >> 'id
//  val branches = l >> 'branches
//
//  implicit lazy val equalInst: Equal[Root] = typeclass.equal
//  implicit lazy val uidInst: UIdL[Root] = UniqueIdL lens id
//
//  implicit lazy val RootArb = Arbitrary(
//    for {
//      n  ← Arbitrary.arbitrary[Name]
//      i  ← Gen choose (1, 10)
//      ls ← Gen listOfN (i, Arbitrary.arbitrary[Branch])
//    } yield Root(Id(0), n, UniqueIdL[Branch,Id] generateIds ls)
//  )
//
//  implicit lazy val RootParent: ParentL[List,Root,Branch,Root :: HNil] =
//    ParentL mplusRoot branches
//}
//
//// vim: set ts=2 sw=2 et:
