package efa.core.syntax

import efa.core.{ValRes, Read, ToXml}

trait ToStringOps {
  implicit class StringOps(val self: String) {
    def read[A:Read]: ValRes[A] = Read[A] read self

    def xml[A:ToXml](a: A): scala.xml.Node = ToXml[A] writeTag (self, a)
  }
}

// vim: set ts=2 sw=2 et:
