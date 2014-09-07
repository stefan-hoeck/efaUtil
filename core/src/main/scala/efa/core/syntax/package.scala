package efa.core

import org.openide.util.Lookup
import scala.xml.Node
import scalaz.Monoid
import scalaz.effect.IO
import scalaz.@>
import std.lookup

package object syntax {
  implicit class localized[A:Localized](val self: A) {
    private def F: Localized[A] = implicitly

    def loc: Localization = F loc self
    def locName: String = F locName self
    def shortName: String = F shortName self
    def desc: String = F desc self
    def names: List[String] = F names self
  }

  implicit class lookup(val self: Lookup) extends AnyVal {
    def head[A:Unerased]: IO[Option[A]] = std.lookup head self
    def all[A:Unerased]: IO[List[A]] = std.lookup all self
  }

  implicit class nodeSeq(val self: Seq[Node]) extends AnyVal {
    def read[A:ToXml]: ValRes[A] = ToXml[A] fromXml self

    def readD[A:ToXml]: DisRes[A] = read[A].disjunction

    def readTag[A:ToXml](tag: String): ValRes[A] =
      ToXml[A] readTag (self, tag)

    def readTagV[A:ToXml](tag: String)(v: EndoVal[A]): ValRes[A] =
      ToXml[A] readTagV (self, tag, v)

    def readTagD[A:ToXml](tag: String): DisRes[A] =
      ToXml[A] readTagD (self, tag)

    def readTags[A:ToXml](tag: String): ValRes[Seq[A]] =
      ToXml[A] readTags (self, tag)

    def readTagsD[A:ToXml](tag: String): DisRes[Seq[A]] =
      ToXml[A] readTagsD (self, tag)

    def readTagO[A:ToXml](tag: String): Option[A] =
      ToXml[A] readTagO (self, tag)

    def readTagWithDefault[A:ToXml:Default](tag: String): A =
      ToXml[A] readTagWithDefault (self, tag)

    def readTagZ[A:ToXml:Monoid](tag: String): A =
      ToXml[A] readTagZ (self, tag)

    def tagged[A:TaggedToXml]: ValRes[A] = TaggedToXml[A] read self
  }

  implicit class string(val self: String) extends AnyVal {
    def read[A:Read]: ValRes[A] = Read[A] read self

    def xml[A:ToXml](a: A): scala.xml.Node = ToXml[A] writeTag (self, a)
  }

  implicit class lens[C,F](val self: C @> F) extends AnyVal {
    import shapeless._
    import ops.hlist.{ At, Init, Last, Prepend, Selector, ReplaceAt,
                       Replacer, Tupler }
    import ops.record.{ Selector => RSelector, Updater }
    import record.{ FieldType, field }

    type Gen[O] = LabelledGeneric.Aux[F, O]
    def >>[Out0 <: HList : Gen, V](k: Witness)(implicit s: RSelector.Aux[Out0, k.T, V], u: Updater.Aux[Out0, FieldType[k.T, V], Out0]) = {
      import shapeless.syntax
      val gen = implicitly[LabelledGeneric.Aux[F, Out0]]

      scalaz.Lens.lensg[C, V](
        (c: C) ⇒ (f: V) ⇒  self.set(c, gen.from(record.recordOps(gen.to(self.get(c))).updated(k, f))),
        (c: C) ⇒ s(gen.to(self.get(c)))
      )
    }
  }
}


// vim: set ts=2 sw=2 et:
