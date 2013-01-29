package efa.core.std

import efa.core.{ToXml, UniqueId}
import org.scalacheck.{Arbitrary, Gen}, Arbitrary.arbitrary
import scala.xml.Node

trait MapFunctions {
  def mapValues[A,B] (m: Map[A,B]): List[B] = m.toList map (_._2)

  def mapArbitrary[A,K] (implicit a: Arbitrary[A], u: UniqueId[A,K])
  : Arbitrary[Map[K,A]] = Arbitrary (mapGen[A,K])

  def mapGen[A,K] (implicit a: Arbitrary[A], u: UniqueId[A,K])
  : Gen[Map[K,A]] = Gen listOf arbitrary[A] map u.idMap

  def mapToXml[A,K](lbl: String)(implicit a: ToXml[A], u: UniqueId[A,K])
  : ToXml[Map[K,A]] = new ToXml[Map[K,A]] {
      val asToXml = ToXml.listToXml[A](lbl)

      def fromXml (ns: Seq[Node]) = asToXml fromXml ns map u.idMap

      def toXml (map: Map[K,A]) = asToXml toXml mapValues (map)
    }
}

object map extends MapFunctions

// vim: set ts=2 sw=2 et:
