package efa.core

import scala.xml.{Node, MetaData, Null, NamespaceBinding, TopScope, Text, XML}
import scalaz._, Scalaz._

trait ToXml[A] {

  def toXml(a: A): Seq[Node]

  def fromXml(ns: Seq[Node]): ValRes[A]

  def attributes (a: A): MetaData = Null

  def scope: NamespaceBinding = TopScope

  def writeTag(t: String, prefix: String, a: A): Node =
    scala.xml.Elem(prefix, t, attributes(a), scope, toXml(a): _*)

  def writeTag(t: String, a: A): Node = writeTag(t, null, a)

  def readTag (
    ns: Seq[Node],
    tag: String,
    default: Option[A] = None
  ): ValRes[A] = ns \ tag match {
    case xs if xs.isEmpty ⇒ default fold (_.success, loc tagNotFoundFail tag)
    case xs ⇒ ToXml adjMessages(tag, fromXml(xs))
  }

  def readTagD (ns: Seq[Node], tag: String): DisRes[A] =
    readTag(ns, tag).disjunction

  def readTagV (ns: Seq[Node], tag: String, v: EndoVal[A]): ValRes[A] =
    readTagD(ns, tag) flatMap v validation

  def readTags (ns: Seq[Node], tag: String): ValRes[Seq[A]] =
    ToXml adjMessages(tag, (ns \ tag toList) traverse fromXml)

  def readTagsD (ns: Seq[Node], tag: String): DisRes[Seq[A]] =
    readTags(ns, tag).disjunction

  //def pretty (a: A): String = toXml(a).shows
}

object ToXml {
  @inline def apply[A:ToXml]: ToXml[A] = implicitly

  val defaultEncoding = "UTF-8"

  def write(n: Node, encoding: String = defaultEncoding): String = {
    val w = new java.io.StringWriter
    XML.write(w, n, encoding, true, null)
    w.toString
  }

  def read[A](toS: A ⇒ String = (a: A) ⇒ a.toString)(implicit r: Read[A])
    : ToXml[A] = new ToXml[A] {
      def toXml(a: A): Seq[Node] = Text(toS(a))
      def fromXml (ns: Seq[Node]): ValRes[A] = r read ns.text
    }

  def readShow[A:Read:Show]: ToXml[A] = read(_.shows)

  def listToXml[A:ToXml] (label: String) = new ToXml[List[A]] {
    private[this] val x = seqToXml[A] (label)
    def toXml (as: List[A]): Seq[Node] = x toXml as
    def fromXml (ns: Seq[Node]): ValRes[List[A]] =
      x fromXml ns map (_.toList)
   }

  def seqToXml[A:ToXml] (label: String) = new ToXml[Seq[A]] {
    def toXml (as: Seq[A]): Seq[Node] =
      as map (ToXml[A] writeTag (label, _))
    def fromXml (ns: Seq[Node]): ValRes[Seq[A]] =
      ToXml[A] readTags (ns, label)
   }

  def streamToXml[A:ToXml] (label: String) = new ToXml[Stream[A]] {
    private[this] val x = seqToXml[A] (label)
    def toXml (as: Stream[A]): Seq[Node] = x toXml as
    def fromXml (ns: Seq[Node]): ValRes[Stream[A]] =
      x fromXml ns map (_.toStream)
   }

  //def treeToXml[A:ToXml] (label: String) = new ToXml[Tree[A]] {

  //  def toXml (as: Tree[A]): Seq[Node] = {
  //    def lbl = ToXml[A] toXml as.rootLabel
  //    def forest: Seq[Node] = as.subForest ∘ (label.xml(_)(this))

  //    lbl ++ forest
  //  }

  //  def fromXml (ns: Seq[Node]): ValRes[Tree[A]] = {
  //    def lbl = ToXml[A] fromXml ns
  //    def forest = (ns \ label).toStream traverse fromXml

  //    ^(lbl, forest) {(l,f) ⇒ node (l, f)}
  //  }
  //}

  private def adjMessages[A](tag: String, v: ValRes[A]): ValRes[A] =
    v.swap ∘ (_ ∘ (tag + ": " + _)) swap
}

trait ToXmlSpecs {
  import Efa._
  import org.scalacheck.Prop

  def writeReadXml[A:Equal:ToXml]: A ⇒ Prop = a ⇒
    compareP(a, (("tag" xml a).read[A]))
}

object ToXmlSpecs extends ToXmlSpecs

// vim: set ts=2 sw=2 et:
