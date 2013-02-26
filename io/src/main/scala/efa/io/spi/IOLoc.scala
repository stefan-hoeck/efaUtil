package efa.io.spi

import java.io.File

trait IOLoc {

  def allFiles: String
  def closed (s: String): String
  def dataReadError: String
  def fileNotFound (s: String): String
  def fileCopied (s: String): String
  def fileCopyError (s: String): String
  def fileCreated (s: String): String
  def fileCreateError (t: Throwable, s: String): String
  def fileCreateUnable (s: String): String
  def fileDeleted (s: String): String
  def fileDeleteError (t: Throwable, s: String): String
  def fileDeleteUnable (s: String): String
  def fileOpenError (s: String): String
  def fileReadError (s: String): String
  def fileWriteError (s: String): String
  def folderCreated (s: String): String
  def folderCreateError (t: Throwable, s: String): String
  def folderCreateUnable (s: String): String
  def folderDeleted (s: String): String
  def folderDeleteError (t: Throwable, s: String): String
  def folderDeleteUnable (s: String): String
  def inputStreamOpened (s: String): String
  def outputStreamOpened (s: String): String
  def resourceNotFound (nameExt: String, cl: Class[_], t: Throwable): String
  def resourceOpened (nameExt: String, cl: Class[_]): String
  def stringWritten (s: String): String
  def throbberMsg(i: Int, millis: Long): String
  def txtExt: String
  def txtFiles: String
  def xmlRead (s: String): String
  def xmlWritten (s: String): String

  final def fileCopied (f: File): String = fileCopied(f.getPath)

  final def fileCopyError (f: File): String = fileCopyError(f.getPath)

  final def fileCreated (f: File): String = fileCreated(f.getPath)

  final def fileCreateError (t: Throwable, f: File): String =
    fileCreateError(t, f.getPath)

  final def fileCreateUnable (f: File): String = fileCreateUnable(f.getPath)

  final def fileDeleted (f: File): String = fileDeleted(f.getPath)

  final def fileDeleteError (t: Throwable, f: File): String =
    fileDeleteError(t, f.getPath)

  final def fileDeleteUnable (f: File): String = fileDeleteUnable(f.getPath)

  final def fileOpenError (f: File): String = fileOpenError(f.getPath)

  final def fileReadError (f: File): String = fileReadError(f.getPath)

  final def fileWriteError (f: File): String = fileWriteError(f.getPath)

  final def folderCreated (f: File): String = folderCreated(f.getPath)

  final def folderCreateError (t: Throwable, f: File): String =
    folderCreateError(t, f.getPath)

  final def folderCreateUnable (f: File): String = folderCreateUnable(f.getPath)

  final def folderDeleted (f: File): String = folderDeleted(f.getPath)

  final def folderDeleteError (t: Throwable, f: File): String =
    folderDeleteError(t, f.getPath)

  final def folderDeleteUnable (f: File): String = folderDeleteUnable(f.getPath)

  final def inputStreamOpened (f: File): String = inputStreamOpened(f.getPath)

  final def outputStreamOpened (f: File): String = outputStreamOpened(f.getPath)
}

object IOLoc extends IOLoc {

  def allFiles = "All Files"

  def closed (s: String) = "Closed " + s

  def dataReadError = "Error when reading data."

  def fileNotFound (s: String) = "File not found: " + s

  def fileCopied (s: String) = "Copied file " + s

  def fileCopyError (s: String) = "Error when copying file " + s

  def fileCreated (s: String) = "File created: " + s

  def fileCreateError (t: Throwable, s: String) =
    "Error when creating file %s: %s" format (s, t.toString)

  def fileCreateUnable (s: String) = "Unable to create file: " + s

  def fileDeleted (s: String) = "File deleted: " + s

  def fileDeleteError (t: Throwable, s: String) =
    "Error when deleting file %s: %s" format (s, t.toString)

  def fileDeleteUnable (s: String) = "Unable to delete file: " + s

  def fileOpenError (s: String) = "Error when opening file " + s

  def fileReadError (s: String) = "Error when reading file " + s

  def fileWriteError (s: String) = "Error when writing file " + s

  def folderNotFound (s: String) = "Folder not found: " + s

  def folderCreated (s: String) = "Folder created: " + s

  def folderCreateError (t: Throwable, s: String) =
    "Error when creating folder %s: %s" format (s, t.toString)

  def folderCreateUnable (s: String) = "Unable to create folder: " + s

  def folderDeleted (s: String) = "Folder deleted: " + s

  def folderDeleteError (t: Throwable, s: String) =
    "Error when deleting folder %s: %s" format (s, t.toString)

  def folderDeleteUnable (s: String) = "Unable to delete folder: " + s

  def inputStreamOpened (s: String): String = "Opened InputStream for " + s

  def outputStreamOpened (s: String): String = "Opened OutputStream for " + s

  def resourceNotFound (nameExt: String, cl: Class[_], t: Throwable) =
    "Resource not found: %s in class %s: %s" format (
      nameExt, cl.getCanonicalName, t.toString)

  def resourceOpened (nameExt: String, cl: Class[_]) =
    "Opened InputStream for %s in class %s." format (
      nameExt, cl.getCanonicalName)

  def stringWritten (s: String) = "Wrote string to " + s

  def throbberMsg(i: Int, millis: Long) = s"Processed $i items in $millis ms"

  def txtExt = "txt"

  def txtFiles = "Text Files [.txt]"

  def xmlRead (s: String) = "Read xml from " + s

  def xmlWritten (s: String) = "Wrote xml to " + s
}

// vim: set ts=2 sw=2 et, nowrap:
