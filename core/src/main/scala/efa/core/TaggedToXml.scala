package efa.core

import scala.xml.{Node, Text}
import scalaz.Show

/** Type class that provides referentially transparent reading from
  * and writing to xml-format.
  *
  * In addition to ToXml's basic functionality, this type class links
  * its type paramete to a fixed tag name.
  */
trait TaggedToXml[A] extends ToXml[A] {
  def tag: String

  final def read(ns: Seq[Node]): ValRes[A] = fromXml (ns \ tag)

  final def write(a: A): Node = writeTag(tag, a)
}

object TaggedToXml {
  def apply[A:TaggedToXml]: TaggedToXml[A] = implicitly

  def read[A](t: String, toS: A ⇒ String = (a: A) ⇒ a.toString)(implicit r: Read[A])
    : TaggedToXml[A] = new TaggedToXml[A] {
      def toXml(a: A): Seq[Node] = Text(toS(a))
      def fromXml (ns: Seq[Node]): ValRes[A] = r read ns.text
      val tag = t
    }

  def readShow[A:Read:Show](t: String): TaggedToXml[A] = read(t, Show[A].shows)
}

// vim: set ts=2 sw=2 et:
