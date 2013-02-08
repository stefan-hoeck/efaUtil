package efa.core.std

import efa.core.{ToXml, UniqueIdL, UniqueId}
import org.scalacheck.{Arbitrary, Gen}, Arbitrary.arbitrary
import scala.xml.Node
import scalaz._, Scalaz._

trait MapFunctions {
  def mapValues[A,B] (m: Map[A,B]): List[B] = m.toList map (_._2)

  def mapArbitrary[A:Arbitrary,K:Enum:Monoid]
    (min: Int = 0, max: Int = 100)
    (implicit u: UniqueIdL[A,K]): Arbitrary[Map[K,A]] =
    Arbitrary(mapGen[A,K](min, max))

  def mapGen[A:Arbitrary,K:Enum:Monoid]
    (min: Int = 0, max: Int = 100)
    (implicit u: UniqueIdL[A,K]): Gen[Map[K,A]] = for {
      i  ← Gen choose (min, max)
      as ← Gen listOfN (i, arbitrary[A]) 
    } yield u.generateIdsMap(as)

  def mapToXml[A,K](lbl: String)(implicit a: ToXml[A], u: UniqueId[A,K])
  : ToXml[Map[K,A]] = new ToXml[Map[K,A]] {
      val asToXml = ToXml.listToXml[A](lbl)

      def fromXml (ns: Seq[Node]) = asToXml fromXml ns map u.idMap[List]

      def toXml (map: Map[K,A]) = asToXml toXml mapValues (map)
    }
}

object map extends MapFunctions

// vim: set ts=2 sw=2 et:
