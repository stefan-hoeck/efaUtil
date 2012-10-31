package efa.core

import scalaz._, Scalaz._

case class Localization(name: String, locName: String, shortName: String,
                        desc: String) {
  def this(name: String, locName: String) =
    this(name, locName, locName, locName)

  def names: List[String] = List(name, locName, shortName, desc)
}

object Localization {
  implicit val LocalizationEqual: Equal[Localization] = Equal.equalA
}

// vim: set ts=2 sw=2 et:
