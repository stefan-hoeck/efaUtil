package efa.io.spi

import java.io.File

trait IOLoc {
  def allFiles: String
  def closed(s: String): String
  def deleteError(s: String, t: Throwable): String
  def fileCreateError(s: String, t: Throwable): String
  def fileCreateUnable(s: String): String
  def fileCreated(s: String): String
  def fileDeleteUnable(s: String): String
  def fileDeleted(s: String): String
  def fileError(s: String, t: Throwable): String
  def fileNotFound(s: String): String
  def folderCreateError(s: String, t: Throwable): String
  def folderCreateUnable(s: String): String
  def folderCreated(s: String): String
  def folderDeleted(s: String): String
  def folderDeleteUnable(s: String): String
  def folderNotFound(s: String): String
  def openError(s: String, t: Throwable): String
  def opened(s: String): String
  def readError(s: String, t: Throwable): String
  def throbberMsg(i: Int, millis: Long): String
  def txtExt: String
  def txtFiles: String
  def writeError(s: String, t: Throwable): String

  final def deleted(f: File) = 
    if(f.isDirectory) folderDeleted(f.getPath) else fileDeleted(f.getPath)

  final def deleteUnable(f: File) = 
    if(f.isDirectory) folderDeleteUnable(f.getPath)
    else fileDeleteUnable(f.getPath)
}

object IOLoc extends IOLoc {
  def allFiles = "All Files"
  def closed(s: String) = s"Closed $s"
  def deleteError (s: String, t: Throwable) = s"Error when deleting $s: $t"
  def fileCreateError (s: String, t: Throwable) = s"Error when creating file $s: $t"
  def fileCreateUnable (s: String) = s"Unable to create file $s"
  def fileCreated (s: String) = s"File created: $s"
  def fileDeleteUnable (s: String) = s"Unable to delete file $s"
  def fileDeleted (s: String) = s"File deleted: $s"
  def fileError(s: String, t: Throwable) = s"Error when creatin file $s: $t"
  def fileNotFound (s: String) = s"File not found: $s"
  def folderCreateError (s: String, t: Throwable) = s"Error when creating folder $s: $t"
  def folderCreateUnable (s: String) = s"Unable to create folder $s"
  def folderCreated (s: String) = s"Folder created: $s"
  def folderDeleteUnable (s: String) = s"Unable to delete folder: $s"
  def folderDeleted (s: String) = s"Folder deleted: $s"
  def folderNotFound (s: String) = s"Folder not found: $s"
  def openError(s: String, t: Throwable) = s"Error when opening $s: $t"
  def opened(s: String) = s"Opened $s"
  def readError(s: String, t: Throwable) = s"Error when reading from $s: $t"
  def throbberMsg(i: Int, millis: Long) = s"Processed $i items in $millis ms"
  def txtExt = "txt"
  def txtFiles = "Text Files [.txt]"
  def writeError(s: String, t: Throwable) = s"Error when writing to $s: $t"

  //def fileCopied (s: String) = "Copied file " + s

  //def fileCopyError (s: String) = "Error when copying file " + s
}

// vim: set ts=2 sw=2 et nowrap:
