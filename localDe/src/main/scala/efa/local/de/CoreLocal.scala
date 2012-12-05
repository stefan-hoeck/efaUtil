package efa.local.de

class CoreLocal extends efa.core.spi.UtilLoc {
  def comment = "Kommentar"
  def commentShort = "Komm."
  def desc = "Beschreibung"
  def descShort = "Besch."
  def isEmptyMsg = "Leere Eingabe"
  def logLevelMsg (value: String): String = "Unbekannter Loglevel: %s" format value
  def maxStringLengthMsg (length: Int) = "Maximal %d Zeichen" format length
  def mustBeEmptyMsg = "Eingabe muss leer sein"
  def name = "Name"
  def stringExists(propName: String, s: String) = "%s %s ist bereits vorhanden" format (propName, s)
  def notAllowedMsg (value: String) = "%s ist keine gültige Eingabe" format value
  def notFoundMsg (value: String) = "Ungültige Eingabe: %s" format value
  def notInIntervalMsg (min: String, max: String) = "Benötigt einen Wert zwischen %s und %s" format (min, max)
  def parseBooleanMsg (s: String) = s + " ist kein Bool'scher Wert (true oder false)"
  def parseFloatMsg (s: String) = s + " ist keine Fliesskommazahl"
  def parseIntMsg (s: String) = s + " ist keine ganze Zahl"
  def tagNotFoundMsg(t: String): String = "%s: XML-Tag nicht gefunden" format t
  def value = "Wert"
  def valueShort = "Wert"
}

// vim: set ts=2 sw=2 nowrap et:
