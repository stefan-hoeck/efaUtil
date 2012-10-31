package efa.local.de

class CoreLocal extends efa.core.spi.UtilLoc {
  def name = "Name"

  def desc = "Beschreibung"

  def descShort = "Besch."

  def value = "Wert"

  def valueShort = "Wert"

  def comment = "Kommentar"

  def commentShort = "Komm."

  def notInIntervalMsg (min: String, max: String) =
    "Benötigt einen Wert zwischen %s und %s" format (min, max)

  def isEmptyMsg = "Leere Eingabe"

  def mustBeEmptyMsg = "Eingabe muss leer sein"

  def maxStringLengthMsg (length: Int) =
    "Maximal %d Zeichen" format length

  def notAllowedMsg (value: String) =
    "%s ist keine gültige Eingabe" format value

  def parseIntMsg (s: String) = s + " ist keine ganze Zahl"

  def parseFloatMsg (s: String) = s + " ist keine Fliesskommazahl"
  
  def parseBooleanMsg (s: String) = s + " ist kein Bool'scher Wert (true oder false)"

  def notFoundMsg (value: String) = "Ungültige Eingabe: %s" format value

  def logLevelMsg (value: String): String =
    "Unbekannter Loglevel: %s" format value

  def tagNotFoundMsg(t: String): String = "%s: XML-Tag nicht gefunden" format t
}

// vim: set ts=2 sw=2 et:
