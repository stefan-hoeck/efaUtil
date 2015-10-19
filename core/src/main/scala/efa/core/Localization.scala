package efa.core

import scalaz._
import scalaz.Scalaz._

/** Provides a couple of localized Strings typically used to describe
  * a property of a data type in different places in a GUI.
  *
  * @param name      The name of the property (should be unique)
  * @param locName   The localized name of the property
  * @param shortName A localized short version of the name.
  *                  Can be used in column headers for instance.
  * @param desc      A short description of the property. Can be used
  *                  in tooltip texts for instance.
  */
case class Localization(
    name: String,
    locName: String,
    shortName: String,
    desc: String) {

  /** A shortcut constructor for Localizations with identical locName,
    * shortName, and desc.
    */
  def this(name: String, locName: String) =
    this(name, locName, locName, locName)

  /** Returns all the strings associated with this `Localization`
    */
  def names: List[String] = List(name, locName, shortName, desc)
}

object Localization {
  implicit val orderInst: Order[Localization] = Order.orderBy {
    case Localization(n,ln,sn,d) ⇒ (n,ln,sn,d)
  }
}

// vim: set ts=2 sw=2 et:
