package efa.core

import Shapeless._
import scalaz._, Scalaz._
import shapeless._, HList._

/**
  * Provides a couple of localized Strings typically used to describe
  * a property of a data type in different places in a GUI.
  *
  * @param name: The name of the property
  * @param locName: The localized name of the property
  * @param shortName: A localized short version of the name.
                      Can be used in column headers for instance.
  * @param desc: A short description of the property. Can be used
                 in tooltip texts for instance.
  */
case class Localization(
    name: String,
    locName: String,
    shortName: String,
    desc: String) {

  /**
    * A shortcut constructor for Localizations with identical locName,
    * shortName, and desc.
    */
  def this(name: String, locName: String) =
    this(name, locName, locName, locName)

  def names: List[String] = List(name, locName, shortName, desc)
}

object Localization {
  implicit val LIso = Iso.hlist(Localization.apply _, Localization.unapply _)
  implicit val LocalizationEqual: Equal[Localization] = ccEqual
}

// vim: set ts=2 sw=2 et:
