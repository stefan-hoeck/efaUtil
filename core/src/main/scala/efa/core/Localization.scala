package efa.core

import Shapeless._
import scalaz._, Scalaz._
import shapeless._, HList._

case class Localization(name: String, locName: String, shortName: String,
                        desc: String) {
  def this(name: String, locName: String) =
    this(name, locName, locName, locName)

  def names: List[String] = List(name, locName, shortName, desc)
}

object Localization {
  implicit val LIso = Iso.hlist(Localization.apply _, Localization.unapply _)
  implicit val LocalizationEqual: Equal[Localization] = ccEqual
}

// vim: set ts=2 sw=2 et:
