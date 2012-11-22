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

  def comment: String
  def commentShort: String
  def desc: String
  def descShort: String
  def isEmptyMsg: String
  def logLevelMsg (value: String): String
  def maxStringLengthMsg (length: Int): String
  def mustBeEmptyMsg: String
  def name: String
  def nameExists (n: String): String
  def notAllowedMsg (value: String): String
  def notFoundMsg (value: String): String
  def notInIntervalMsg (min: String, max: String): String
  def parseBooleanMsg (s: String): String
  def parseFloatMsg (s: String): String
  def parseIntMsg (s: String): String
  def tagNotFoundMsg (s: String): String
  def value: String
  def valueShort: String

  final def tagNotFoundFail[A] (s: String): ValRes[A] =
    tagNotFoundMsg(s).failureNel
} 

/**
 * Default localization (English)
 */
object UtilLoc extends UtilLoc {
  
  def comment = "Comment"
  def commentShort = "Comm."
  def desc = "Description"
  def descShort = "Desc."
  def isEmptyMsg = "Requires a non-empty string"

  def logLevelMsg (value: String): String =
    "Unknown log level: %s" format value

  def maxStringLengthMsg (length: Int) =
    "Maximum string length is %d" format length

  def mustBeEmptyMsg = "Requires an empty string"
  def name = "Name"
  def nameExists (s: String) = "Name %s already exists" format s
  def notAllowedMsg (value: String) = "%s is not a valid value" format value
  def notFoundMsg (value: String) = "Unknown input: %s" format value

  def notInIntervalMsg (min: String, max: String) =
    "Requires a value between %s and %s" format (min, max)

  def parseBooleanMsg (s: String) = s + " is not a boolean (true or false)"
  def parseFloatMsg (s: String) = s + " is not a floating point number"
  def parseIntMsg (s: String) = s + " is not an integer"
  def tagNotFoundMsg (s: String): String = "%s: XML-Tag not found" format s
  def value = "Value"
  def valueShort = "Val."

}

// vim: set ts=2 sw=2 et:
