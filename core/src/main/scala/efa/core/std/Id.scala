package efa.core.std

import efa.core.TaggedToXml
import scala.xml.Node

trait IdFunctions {
  def toXml[A:TaggedToXml](a: A): Seq[Node] = TaggedToXml[A] write a
}

object id extends IdFunctions

// vim: set ts=2 sw=2 et:
