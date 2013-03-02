package efa.local.de

import efa.io.spi.IOLoc

class IoLocal extends IOLoc {
  def allFiles = "Alle Dateien"
  def closed(s: String) = s"$s geschlossen"
  def copied(from: String, to: String) = s"$from nach $to kopiert."
  def deleteError(s: String, t: Throwable) = s"Fehler beim Löschen von $s: $t"
  def fileCreateError(s: String, t: Throwable) = s"Fehler beim Erstellen von Datei $s: $t"
  def fileCreateUnable(s: String) = s"Erstellen von Datei %s nicht möglich."
  def fileCreated(s: String) = s"Datei $s wurde erstellt."
  def fileDeleteUnable(s: String) = s"Löschen von Datei $s nicht möglich."
  def fileDeleted(s: String) = s"Datei $s wurde gelöscht"
  def fileError(s: String, t: Throwable) = s"Fehler beim Zuweisen von Datei $s: $t"
  def fileNotFound(s: String) = s"Datei nicht gefunden: $s"
  def folderCreateError(s: String, t: Throwable) = s"Fehler beim Erstellen von Ordner $s: $t"
  def folderCreateUnable(s: String) = s"Erstellen von Ordner $s unmöglich."
  def folderCreated(s: String) = s"Ordner $s wurde erstellt."
  def folderDeleteUnable(s: String) = "Löschen von Ordner %s unmöglich." format s
  def folderDeleted(s: String) = s"Ordner $s wurde gelöscht."
  def folderNotFound(s: String) = s"Ordner nicht gefunden: $s"
  def openError(s: String, t: Throwable) = s"Fehler beim Öffnen von $s: $t"
  def opened(s: String) = s"$s geöffnet"
  def readError(s: String, t: Throwable) = s"Fehler beim Lesen von $s: $t"
  def throbberMsg(i: Int, millis: Long) = s"$i Elemente in $millis ms verarbeitet"
  def txtExt = "txt"
  def txtFiles = "Textdateien [.txt]"
  def writeError(s: String, t: Throwable) = s"Fehler beim Schreiben nach $s: $t"
}

// vim: set ts=2 sw=2 et nowrap:
