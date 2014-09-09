package efa.core

import scala.language.experimental.macros
import scala.xml.{Node, MetaData, Null, NamespaceBinding, TopScope, Text, XML}
import scalaz._, Scalaz._
import shapeless.{LabelledProductTypeClass, HNil, HList, GenericMacros, :: ⇒ HCons}

/** Type class that provides referentially transparent reading from
  * and writing to xml-format.
  */
trait ToXml[A] {

  def toXml(a: A): Seq[Node]

  def fromXml(ns: Seq[Node]): ValRes[A]

  def attributes (a: A): MetaData = Null

  def scope: NamespaceBinding = TopScope

  def writeTag(t: String, prefix: String, a: A): Node =
    scala.xml.Elem(prefix, t, attributes(a), scope, true, toXml(a): _*)

  def writeTag(t: String, a: A): Node = writeTag(t, null, a)

  def readTag(
    ns: Seq[Node],
    tag: String,
    default: Option[A] = None): ValRes[A] = ns \ tag match {
      case xs if xs.isEmpty ⇒ default toSuccess loc.tagNotFoundMsg(tag).wrapNel
      case xs ⇒ ToXml adjMessages(tag, fromXml(xs))
    }

  def readTagD(ns: Seq[Node], tag: String): DisRes[A] =
    readTag(ns, tag).disjunction

  def readTagV(ns: Seq[Node], tag: String, v: EndoVal[A]): ValRes[A] =
    readTagD(ns, tag) flatMap v validation

  def readTags(ns: Seq[Node], tag: String): ValRes[Seq[A]] =
    ToXml adjMessages(tag, (ns \ tag toList) traverse fromXml)

  def readTagsD(ns: Seq[Node], tag: String): DisRes[Seq[A]] =
    readTags(ns, tag).disjunction

  def readTagO(ns: Seq[Node], tag: String): Option[A] =
    readTagD(ns, tag).toOption

  def readTagWithDefault(ns: Seq[Node], tag: String)(implicit A: Default[A])
    :A = readTagD(ns, tag).fold(_ ⇒ A.default, identity)

  def readTagZ(ns: Seq[Node], tag: String)(implicit A: Monoid[A])
    :A = readTagD(ns, tag).fold(_ ⇒ A.zero, identity)
}

object ToXml extends ToXmlSpecs {
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

  implicit val productTCInst: LabelledProductTypeClass[ToXml] =
    new LabelledProductTypeClass[ToXml] {
      val emptyProduct: ToXml[HNil] = new ToXml[HNil]{
        def toXml(h: HNil): Seq[Node] = Nil
        def fromXml(ns: Seq[Node]): ValRes[HNil] = HNil.success
      }

      def project[F,G](inst: ⇒ ToXml[G], to: F ⇒ G, from: G ⇒ F): ToXml[F] =
        new ToXml[F] {
          def toXml(f: F) = inst toXml to(f)
          def fromXml(ns: Seq[Node]) = inst fromXml ns map from
        }

      def product[H,T <: HList](lbl: String, ch: ToXml[H], ct: ToXml[T])
        : ToXml[HCons[H,T]] = new ToXml[HCons[H,T]] {
          def toXml(ht: HCons[H,T]): Seq[Node] =
            ch.writeTag(lbl, ht.head) ++ ct.toXml(ht.tail)

          def fromXml(ns: Seq[Node]): ValRes[HCons[H,T]] = 
            ^(ch.readTag(ns, lbl), ct.fromXml(ns))(HCons.apply)
        }
    }

  def derive[A](implicit ev: LabelledProductTypeClass[ToXml]): ToXml[A] =
    macro GenericMacros.deriveLabelledProductInstance[ToXml, A]

  def listToXml[A:ToXml](label: String) = new ToXml[List[A]] {
    private[this] val x = seqToXml[A] (label)
    def toXml (as: List[A]): Seq[Node] = x toXml as
    def fromXml (ns: Seq[Node]): ValRes[List[A]] =
      x fromXml ns map (_.toList)
   }

  def nelToXml[A:ToXml](label: String) = new ToXml[Nel[A]] {
    private[this] val x = listToXml[A] (label)
    def toXml(as: Nel[A]): Seq[Node] = x toXml as.list
    def fromXml(ns: Seq[Node]): ValRes[Nel[A]] =
      (x fromXml ns).disjunctioned (_ flatMap toNel)

    private def toNel(as: List[A]): DisRes[Nel[A]] = as match {
      case Nil ⇒ loc.listMustNotBeEmpty.wrapNel.left
      case x :: xs ⇒ NonEmptyList[A](x, xs: _*).right
    }
   }

  def seqToXml[A:ToXml](label: String) = new ToXml[Seq[A]] {
    def toXml (as: Seq[A]): Seq[Node] =
      as map (ToXml[A] writeTag (label, _))
    def fromXml (ns: Seq[Node]): ValRes[Seq[A]] =
      ToXml[A] readTags (ns, label)
   }

  def streamToXml[A:ToXml](label: String) = new ToXml[Stream[A]] {
    private[this] val x = seqToXml[A] (label)
    def toXml (as: Stream[A]): Seq[Node] = x toXml as
    def fromXml (ns: Seq[Node]): ValRes[Stream[A]] =
      x fromXml ns map (_.toStream)
   }

  private def adjMessages[A](tag: String, v: ValRes[A]): ValRes[A] =
    v.swap ∘ (_ ∘ (tag + ": " + _)) swap
}

trait ToXmlSpecs {
  import Efa._, syntax._
  import org.scalacheck.{Prop, Arbitrary}

  def laws[A:Equal:ToXml:Arbitrary]: Prop = Prop forAll { a: A ⇒
    compareP(a, (("tag" xml a).read[A]))
  }
}

// vim: set ts=2 sw=2 et:
