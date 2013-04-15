package efa.io

import java.io._
import logDisIO._
import scalaz._, Scalaz._, effect.IO

trait AsFile[A] extends AsInput[A] with AsOutput[A] {
  import AsFile.FileAsFile

  override protected def is(a: A) = 
    fileIO(a) >>= (f ⇒ IO(new FileInputStream(f)))

  override protected def os(a: A) = 
    fileIO(a) >>= (f ⇒ IO(new FileOutputStream(f)))

  //overriden so it tries to create the file if it does not exist.
  override def outputStream(a: A) = for {
    f ← create(a)
    o ← except(point(new FileOutputStream(f)), loc openError (name(a), _))
    _ ← debug(loc opened name(a))
  } yield o

  protected def fileIO(a: A): IO[File]

  /** Creates a `java.io.File` from an `A` */
  final def file(a: A): LogDisIO[File] =
    except(liftIO(fileIO(a)), fileError(a))

  private def fileError(a: A)(t: Throwable): String =
    loc fileError (name(a), t)

  /** Checks whether a `File` exists and returns either
    * the `File` or an error message
    */
  def existingFile(a: A): LogDisIO[File] = for {
    f ← file(a)
    r ← f.exists ? success(f) | fail(loc fileNotFound name(a))
  } yield r

  /** Returns a file from an existing file and a relative path
    */
  def file(a: A, path: String): LogDisIO[File] =
    file(a) >>= { f ⇒ AsFile[File].file(new File(f, path)) }

  def create(a: A): LogDisIO[File] = {
    def run(f: File) = for {
      b ← success(f.createNewFile)
      _ ← b ? debug(loc fileCreated name(a)) |
              warning(loc fileCreateUnable name(a))
    } yield f

    tryWithNonExisting(run, a, loc.fileCreateError)
  }

  /** Returns a file from a given folder. Both, File and Folder are
    * created on disk if they don't exist.
    */
  def addFile(folder: A, nameExt: String): LogDisIO[File] = for {
    fo ← mkdirs(folder)
    fi ← AsFile[File].file(fo, nameExt) >>= AsFile[File].create
  } yield fi

  /** Returns a given folder from within another folder. Any folder
    * that does not already exist is created on disk.
    */
  def addDirs(folder: A, path: String): LogDisIO[File] = for {
    fo ← mkdirs(folder)
    fi ← AsFile[File].file(fo, path) >>= AsFile[File].mkdirs
  } yield fi

  def delete(a: A): LogDisIO[Unit] = {
    def run(f: File) = for {
      b ← success(f.delete())
      _ ← b ? debug(loc deleted f) | warning(loc deleteUnable f)
    } yield ()

    tryWithFile(run, a, loc.deleteError)
  }

  /**
   * Creates the folders for the given A recursively and returns the
   * file wrapped in the IO-Monad.
   */
  def mkdirs(a: A): LogDisIO[File] = {
    def run(f: File) = for {
      b ← success(f.mkdirs)
      _ ← b ? debug(loc folderCreated name(a)) |
              warning(loc folderCreateUnable name(a))
    } yield f

    tryWithNonExisting(run, a, loc.folderCreateError)
  }

  def tryWithFile[B](
    f: File ⇒ LogDisIO[B],
  a: A, msg: (String, Throwable) ⇒ String): LogDisIO[B] =
  file(a) >>= { fi ⇒ except(f(fi), msg.curried(name(a))) }

def tryWithNonExisting
  (f: File ⇒ LogDisIO[File], a: A, msg: (String, Throwable) ⇒ String)
  : LogDisIO[File] =
  tryWithFile(fi ⇒ if (fi.exists) success(fi) else f(fi), a, msg)
}

trait AsFileSyntax extends AsInputSyntax with AsOutputSyntax {
  implicit class AsFileOps[A](a: A)(implicit F:AsFile[A]) {
    def file: LogDisIO[File] = F file a

    def existingFile: LogDisIO[File] = F existingFile a

    def fileAt(path: String): LogDisIO[File] = F.file(a, path)

    def create: LogDisIO[File] = F create a

    def addFile(nameExt: String): LogDisIO[File] = F.addFile(a, nameExt)

    def addDirs(path: String): LogDisIO[File] = F.addDirs(a, path)

    def delete: LogDisIO[Unit] = F delete a

    def mkdirs: LogDisIO[File] = F mkdirs a
  }
}

trait AsFileInstances {
  implicit val FileAsFile: AsFile[File] = new AsFile[File]{ 
    protected def fileIO(f: File) = IO(f)
    def name(f: File) = f.getPath
  }

  implicit val StringAsFile: AsFile[String] = new AsFile[String]{
    protected def fileIO(s: String) = IO(new File(s))
    def name(s: String) = s
  }
}

object AsFile extends AsFileInstances {
  def apply[A:AsFile]: AsFile[A] = implicitly

  object syntax extends AsFileSyntax
}

// vim: set ts=2 sw=2 et:
