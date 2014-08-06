package efa.core.spi

import efa.core.{ValRes, Localization, Default, Unerased}
import scalaz.syntax.validation._
import scala.reflect.classTag

trait UtilLoc {
  lazy val commentLoc =
    Localization("comment", comment, commentShort, comment)

  lazy val descLoc =
    Localization("desc", desc, descShort, desc)

  lazy val valueLoc =
    Localization("value", value, valueShort, value)

  def comment: String
  def commentShort: String
  def desc: String
  def descShort: String
  def isEmptyMsg: String
  def listMustNotBeEmpty: String
  def logLevelMsg (value: String): String
  def maxStringLengthMsg (length: Int): String
  def mustBeEmptyMsg: String
  def name: String
  def notAllowedMsg (value: String): String
  def notFoundMsg (value: String): String
  def notInIntervalMsg (min: String, max: String): String
  def parseBooleanMsg (s: String): String
  def parseFloatMsg (s: String): String
  def parseIntMsg (s: String): String
  def stringExists (propName: String, n: String): String
  def tagNotFoundMsg (s: String): String
  def value: String
  def valueShort: String
} 

/**
 * Default localization (English)
 */
object UtilLoc extends UtilLoc {
  // Necessary as macro of this project cannot yet be expanded
  implicit val lookupableImpl: Unerased[UtilLoc] =
    Unerased.unsafe(classTag[UtilLoc])

  implicit val defaultImpl: Default[UtilLoc] = Default.default(this)

  def comment = "Comment"
  def commentShort = "Comm."
  def desc = "Description"
  def descShort = "Desc."
  def isEmptyMsg = "Requires a non-empty string"
  def listMustNotBeEmpty = "List must not be empty"
  def logLevelMsg (value: String): String = "Unknown log level: %s" format value
  def maxStringLengthMsg (length: Int) = "Maximum string length is %d" format length
  def mustBeEmptyMsg = "Requires an empty string"
  def name = "Name"
  def nameExists (s: String) = "Name %s already exists" format s
  def notAllowedMsg (value: String) = "%s is not a valid value" format value
  def notFoundMsg (value: String) = "Unknown input: %s" format value
  def notInIntervalMsg (min: String, max: String) = "Requires a value between %s and %s" format (min, max)
  def parseBooleanMsg (s: String) = s + " is not a boolean (true or false)"
  def parseFloatMsg (s: String) = s + " is not a floating point number"
  def parseIntMsg (s: String) = s + " is not an integer"
  def stringExists (propName: String, s: String) = "%s %s already exists" format (propName, s)
  def tagNotFoundMsg (s: String): String = "%s: XML-Tag not found" format s
  def value = "Value"
  def valueShort = "Val."

}

// vim: set ts=2 sw=2 nowrap et:
