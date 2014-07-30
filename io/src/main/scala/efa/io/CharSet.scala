package efa.io

final case class CharSet private(value: String)

object CharSet {
  implicit def ToStringFromCharSet(charSet:CharSet): String = charSet.value

  val USASCII = CharSet("US-ASCII")

  val ISO8859 = CharSet("ISO-8859-1")

  val UTF8 = CharSet("UTF-8")

  val UTF16BE = CharSet("UTF-16BE")

  val UTF16LE = CharSet("UTF-16LE")

  val UTF16 = CharSet("UTF-16")
}

// vim: set ts=2 sw=2 et:
