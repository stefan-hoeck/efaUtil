package efa.core.syntax

import efa.core.{ToXml, TaggedToXml, ValRes, DisRes, EndoVal, Default}
import scala.xml.Node
import scalaz.Monoid

trait ToNodeSeqOps {
  implicit class NodeSeqOps(val self: Seq[Node]) {
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
}

// vim: set ts=2 sw=2 et: