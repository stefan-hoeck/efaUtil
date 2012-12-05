package efa.local.de

import efa.io.spi.IOLoc

class IoLocal extends IOLoc {
  def allFiles = "Alle Dateien"

  def closed (s: String) = s + " geschlossen"

  def dataReadError = "Fehler beim Lesen von Daten"

  def fileNotFound (s: String) = "Datei nicht gefunden: " + s

  def fileCopied (s: String) = "Datei kopiert " + s

  def fileCopyError (s: String) = "Fehler beim Kopieren von Datei " + s

  def fileCreated (s: String) = "Datei erstellt: " + s

  def fileCreateError (t: Throwable, s: String) =
    "Fehler beim Erstellen von Datei %s: %s" format (s, t.toString)

  def fileCreateUnable (s: String) = "Erstellen von Datei %s unmöglich." format s

  def fileDeleted (s: String) = "Datei gelöscht: " + s

  def fileDeleteError (t: Throwable, s: String) =
    "Fehler beim Löschen von Datei %s: %s" format (s, t.toString)

  def fileDeleteUnable (s: String) = "Löschen von Datei %s unmöglich." format s

  def fileOpenError (s: String) = "Fehler beim Öffnen von Datei " + s

  def fileReadError (s: String) = "Fehler beim Lesen von Datei " + s

  def fileWriteError (s: String) = "Fehler beim Schreiben von Datei " + s

  def folderNotFound (s: String) = "Ordner nicht gefunden: " + s

  def folderCreated (s: String) = "Ordner erstellt: " + s

  def folderCreateError (t: Throwable, s: String) =
    "Fehler beim Erstellen von Ordner %s: %s" format (s, t.toString)

  def folderCreateUnable (s: String) = "Erstellen von Ordner %s unmöglich." format s

  def folderDeleted (s: String) = "Ordner gelöscht: " + s

  def folderDeleteError (t: Throwable, s: String) =
    "Fehler beim Löschen von Ordner %s: %s" format (s, t.toString)

  def folderDeleteUnable (s: String) = "Löschen von Ordner %s unmöglich." format s

  def inputStreamOpened (s: String): String = "InputStream für %s geöffnet" format s

  def outputStreamOpened (s: String): String = "OutputStream für %s geöffnet" format s

  def resourceNotFound (nameExt: String, cl: Class[_], t: Throwable) =
    "Resourse %s in Klasse %s nicht gefunden: %s" format (
      nameExt, cl.getCanonicalName, t.toString)

  def resourceOpened (nameExt: String, cl: Class[_]) =
    "InputStream für %s in Klasse %s geöffnet." format (
      nameExt, cl.getCanonicalName)

  def stringWritten (s: String) = "Text geschrieben nach " + s

  def txtExt = "txt"

  def txtFiles = "Textdateien [.txt]"

  def xmlRead (s: String) = "Xml gelesen von " + s

  def xmlWritten (s: String) = "Xml geschrieben nach " + s
} /// end of IoLocal

// vim: set ts=2 sw=2 et:
