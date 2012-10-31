package efa.core.syntax

import efa.core.{Localized, Localization}
import scalaz.syntax.Ops

trait LocalizedOps[A] extends Ops[A] {
  implicit def F: Localized[A]

  def loc: Localization = F loc self
  def locName: String = F locName self
  def shortName: String = F shortName self
  def desc: String = F desc self
  def names: List[String] = F names self
}

trait ToLocalizedOps {
  implicit def ToLocalizedOps[A:Localized](a: A): LocalizedOps[A] =
    new LocalizedOps[A]{def self = a; def F = Localized[A]}
}

// vim: set ts=2 sw=2 et:
