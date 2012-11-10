package efa.core.spi

import efa.core.{ValRes, Localization}
import scalaz.syntax.validation._

trait UtilLoc {
  lazy val commentLoc =
    Localization("comment", comment, commentShort, comment)

  lazy val descLoc =
    Localization("desc", desc, descShort, desc)

  lazy val valueLoc =
    Localization("value", value, valueShort, value)

  def name: String

  def desc: String

  def descShort: String

  def value: String

  def valueShort: String

  def comment: String

  def commentShort: String

  def notInIntervalMsg (min: String, max: String): String

  def isEmptyMsg: String

  def mustBeEmptyMsg: String

  def maxStringLengthMsg (length: Int): String

  def notAllowedMsg (value: String): String

  def parseIntMsg (s: String): String

  def parseFloatMsg (s: String): String

  def parseBooleanMsg (s: String): String

  def notFoundMsg (value: String): String

  def logLevelMsg (value: String): String

  def tagNotFoundMsg (s: String): String

  final def tagNotFoundFail[A] (s: String): ValRes[A] =
    tagNotFoundMsg(s).failureNel
} 

/**
 * Default localization (English)
 */
object UtilLoc extends UtilLoc {
  def name = "Name"

  def desc = "Description"

  def descShort = "Desc."

  def value = "Value"

  def valueShort = "Val."

  def comment = "Comment"

  def commentShort = "Comm."

  def notInIntervalMsg (min: String, max: String) =
    "Requires a value between %s and %s" format (min, max)

  def isEmptyMsg = "Requires a non-empty string"

  def mustBeEmptyMsg = "Requires an empty string"

  def maxStringLengthMsg (length: Int) =
    "Maximum string length is %d" format length

  def notAllowedMsg (value: String) =
    "%s is not a valid value" format value

  def parseIntMsg (s: String) = s + " is not an integer"

  def parseFloatMsg (s: String) = s + " is not a floating point number"
  
  def parseBooleanMsg (s: String) = s + " is not a boolean (true or false)"

  def notFoundMsg (value: String) = "Unknown input: %s" format value

  def logLevelMsg (value: String): String =
    "Unknown log level: %s" format value

  def tagNotFoundMsg (s: String): String = "%s: XML-Tag not found" format s
}

// vim: set ts=2 sw=2 et:
