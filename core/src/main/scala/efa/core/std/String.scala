package efa.core.std

import efa.core._
import scalaz.{Scalaz, NonEmptyList}, Scalaz._
import scala.xml._

trait StringInstances {
  implicit val StringRead = Read read Validators.dummy[String]

  implicit val StringToXml: ToXml[String] = ToXml read identity

  implicit val StringDefault: Default[String] = Default.monoid[String]
}

object string extends StringInstances

// vim: set ts=2 sw=2 et:
