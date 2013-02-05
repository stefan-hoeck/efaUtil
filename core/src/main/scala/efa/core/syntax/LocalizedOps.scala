package efa.core.syntax

import efa.core.{Localized, Localization}

trait ToLocalizedOps {
  implicit class LocalizedOps[A:Localized](val self: A) {
    private def F: Localized[A] = implicitly

    def loc: Localization = F loc self
    def locName: String = F locName self
    def shortName: String = F shortName self
    def desc: String = F desc self
    def names: List[String] = F names self
  }
}

// vim: set ts=2 sw=2 et:
