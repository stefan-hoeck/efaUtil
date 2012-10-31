package efa.core.syntax

import efa.core._
import scalaz.syntax.Ops

trait StringOps extends Ops[String] {
  def read[A:Read]: ValRes[A] = Read[A] read self

  def xml[A:ToXml](a: A): scala.xml.Node = ToXml[A] writeTag (self, a)
}

trait ToStringOps {
  implicit def ToStringOps(a: String): StringOps =
    new StringOps{def self = a}
}

// vim: set ts=2 sw=2 et:
