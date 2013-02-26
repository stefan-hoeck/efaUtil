package efa.io

import java.io._
import valLogIO._
import scalaz._, Scalaz._

trait AsFile[-A] extends AsInput[A] with AsOutput[A] {
  import AsFile.FileAsFile

  override def inputStream(a: A): ValLogIO[InputStream] = for {
    f  ← file(a)
    is ← except(success(new FileInputStream(f)), _ ⇒ readError(a))
  } yield is

  override def outputStream(a: A): ValLogIO[OutputStream] = for {
    f  ← create(a)
    is ← except(success(new FileOutputStream(f)), _ ⇒ writeError(a))
  } yield is

  override def readError(a: A): String = loc fileReadError path(a)

  override def writeError(a: A): String = loc fileWriteError path(a)

  /** Tries to create a `java.io.File` from an `A` */
  def file(a: A): ValLogIO[File]

  /** Checks whether a `File` exists and returns either
    * the `File` or an error message
    */
  def existingFile(a: A): ValLogIO[File] = for {
    f ← file(a)
    r ← f.exists ? success(f) | fail(loc fileNotFound path(a))
  } yield r

  /** Returns a file 
    * the `File` or an error message
    */
  def file(a: A, path: String): ValLogIO[File] =
    file(a) >>= { f ⇒ success(new File(f, path)) }

  def path(a: A): String

  def create(a: A): ValLogIO[File] = {
    def run(f: File) = for {
      b ← success(f.createNewFile)
      _ ← b ? debug (loc fileCreated f) | warning (loc fileCreateUnable f)
    } yield f

    tryWithNonExisting(run, a, loc.fileCreateError)
  }

  /** Returns a file from a given folder. Both, File and Folder are
    * created on disk if they don't exist.
    */
  def addFile(folder: A, nameExt: String): ValLogIO[File] = for {
    fo ← mkdirs(folder)
    fi ← AsFile[File].file(fo, nameExt) >>= AsFile[File].create
  } yield fi

  /** Returns a given folder from within another folder. Any folder
    * that does not already exist is created on disk.
    */
  def addDirs(folder: A, path: String): ValLogIO[File] = for {
    fo ← mkdirs(folder)
    fi ← AsFile[File].file(fo, path) >>= AsFile[File].mkdirs
  } yield fi

  def delete(a: A): ValLogIO[Unit] = {
    def run(f: File) = for {
      b ← success(f.delete())
      _ ← b ? debug (loc fileDeleted f) | warning (loc fileDeleteUnable f)
    } yield ()

    tryWithFile(run, a, loc.fileDeleteError)
  }

  /**
   * Creates the folders for the given A recursively and returns the
   * file wrapped in the IO-Monad.
   */
  def mkdirs(a: A): ValLogIO[File] = {
    def run(f: File) = for {
      b ← success(f.mkdirs)
      _ ← b ? debug (loc folderCreated f) | warning (loc folderCreateUnable f)
    } yield f

    tryWithNonExisting(run, a, loc.folderCreateError)
  }

  def tryWithFile[B]
    (f: File ⇒ ValLogIO[B], a: A, msg: (Throwable, String) ⇒ String)
    : ValLogIO[B] =
    file(a) >>= { fi ⇒ except(f(fi), msg(_, path(a))) }

  def tryWithNonExisting
    (f: File ⇒ ValLogIO[File], a: A, msg: (Throwable, String) ⇒ String)
    : ValLogIO[File] =
    tryWithFile(fi ⇒ if (fi.exists) success(fi) else f(fi), a, msg)
}

trait AsFileInstances {
  implicit val FileAsFile: AsFile[File] = new AsFile[File]{ 
    def file(f: File) = point(f)
    def path(f: File) = f.getPath
  }

  implicit val StringAsFile: AsFile[String] = new AsFile[String]{
    def file(s: String) = success(new File(s))
    def path(s: String) = s
  }
}

object AsFile extends AsFileInstances {
  def apply[A:AsFile]: AsFile[A] = implicitly
}

// vim: set ts=2 sw=2 et:
