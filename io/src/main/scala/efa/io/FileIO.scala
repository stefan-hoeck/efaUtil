package efa.io

//import scalaz._, Scalaz._, effect._
//import efa.core._, Efa._
//import java.io.{IOException, OutputStream, InputStream, Reader,
//  Writer, OutputStreamWriter, File, FileOutputStream, FileInputStream,
//  InputStreamReader}
//
//object FileIO {
//  import valLogIO._, resource._
//
//  def file (path: String): ValLogIO[File] = success (new File(path))
//
//  def existingFile (path: String): ValLogIO[File] = for {
//    f ← file(path)
//    r ← f.exists ? success(f) | fail(loc fileNotFound path)
//  } yield r
//
//  def file (folder: ⇒ File, path: String): ValLogIO[File] =
//    success (new File (folder, path))
//
//  /**
//   * Returns a File for the given path. The file is created, if it did not
//   * exist already.
//   */
//  def createFile (f: String): ValLogIO[File] = file(f) >>= (createFile (_))
//  
//  /**
//   * Creates a new File if it did not exist already.
//   * No errors are thrown. Results in a Failure (see Validation)
//   * if the File could not be created.
//   */
//  def createFile (f: ⇒ File): ValLogIO[File] = {
//    def create = for {
//      b ← success(f.createNewFile)
//      _ ← b ? debug (loc fileCreated f) | warning (loc fileCreateUnable f)
//    } yield f
//
//    except (f.exists ? success (f) | create, loc.fileCreateError(_, f))
//  }
//
//  /**
//   * Returns a file from a given folder. Both, File and Folder are
//   * created on disk if they don't exist.
//   */
//  def addFile (folder: File, nameExt: String): ValLogIO[File] = 
//    mkdirs (folder) ∗ (file (_, nameExt)) ∗ (createFile (_))
//
//  /**
//   * Returns a given folder from within another folder. Any folder
//   * that does not already exist is created on disk.
//   */
//  def addDirs (folder: File, path: String): ValLogIO[File] = 
//    mkdirs (folder) ∗ (file (_, path)) ∗ (mkdirs)
//
//  def deleteFile (s: String): ValLogIO[Unit] = file(s) ∗ (deleteFile)
//
//  def deleteFile (f: File): ValLogIO[Unit] = {
//    def delete = for {
//      b ← success(f.delete())
//      _ ← b ? debug (loc fileDeleted f) | warning (loc fileDeleteUnable f)
//    } yield ()
//
//    except (delete, loc.fileDeleteError(_, f))
//  }
//
//  /**
//   * Returns a folder representing the given path. All directories leading
//   * to that folder are created recursively.
//   */
//  def mkdirs (f: String): ValLogIO[File] = file(f) ∗ (mkdirs)
//
//  /**
//   * Creates the folders for the given File recursively and returns the
//   * file wrapped in the IO-Monad.
//   */
//  def mkdirs (f: File): ValLogIO[File] = {
//    def create = for {
//      b ← success(f.mkdirs)
//      _ ← b ? debug (loc folderCreated f) | warning (loc folderCreateUnable f)
//    } yield f
//
//    except (f.exists ? success (f) | create, loc.folderCreateError(_, f))
//  }
//
//  /**
//   * Closes a given Close
//   */
//   def close[A:Resource](c: ⇒ A): ValLogIO[Unit] = {
//     def cl: IO[Unit] = Resource[A] close c except (_ ⇒ IO.ioUnit)
//
//     liftIO(cl) >> trace (loc closed c.toString)
//   }
//
//  /**
//   * Performs some action with a given Close. Exceptions are caught and
//   * wrapped as messages in a ValRes. The given Close is closed in
//   * the end, no matter whether an exception was raised or not.
//   */
//  def withClose[C:Resource,A] (c: ⇒ C, msg: ⇒ String)(f: C ⇒ ValLogIO[A])
//    : ValLogIO[A] = {
//      val eMsg = (t: Throwable) ⇒  msg + ": " + t.toString
//
//      ensure (except (f(c), eMsg), close (c))
//    }
//
//  def fileInputStream (path: String): ValLogIO[InputStream] =
//    file(path) ∗ (f ⇒ fileInputStream(f))
//
//  def fileInputStream (f: ⇒ File): ValLogIO[InputStream] = {
//    def open = for {
//      is ← success[InputStream](new FileInputStream(f)) 
//      _  ← trace (loc inputStreamOpened f)
//    } yield is
//
//    except (open, _ ⇒ loc fileOpenError f)
//  }
//
//  def fileOutputStream (path: String): ValLogIO[OutputStream] =
//    file(path) ∗ (f ⇒ fileOutputStream(f))
//
//  def fileOutputStream (f: ⇒ File): ValLogIO[OutputStream] = {
//    def open = for {
//      os ← success[OutputStream](new FileOutputStream(f)) 
//      _  ← trace (loc outputStreamOpened f)
//    } yield os
//
//    except (open, _ ⇒ loc fileOpenError f)
//  }
//
//  def withInputStream[A] (path: String)(f: InputStream ⇒ ValLogIO[A])
//    : ValLogIO[A] = file(path) >>= (fi ⇒ withInputStream(fi)(f))
//
//  def withInputStream[A] (file: ⇒ File)(f: InputStream ⇒ ValLogIO[A])
//    : ValLogIO[A] = for {
//      fis ← fileInputStream(file)
//      a   ← withInputStream(fis, loc fileReadError file)(f)
//    } yield a
//
//  def withInputStream[A] (in: ⇒ InputStream, msg: ⇒ String)
//    (f: InputStream ⇒ ValLogIO[A]) : ValLogIO[A] = withClose(in, msg)(f)
//
//  def withOutputStream (path: String)(f: OutputStream ⇒ ValLogIO[Unit])
//    : ValLogIO[Unit] = {
//      createFile(path) ∗ (withOutputStream (_)(f))
//    }
//
//  def withOutputStream (file: ⇒ File)(f: OutputStream ⇒ ValLogIO[Unit])
//    : ValLogIO[Unit] = for {
//      fos ← fileOutputStream(file)
//      a   ← withOutputStream(fos, loc fileWriteError file)(f)
//    } yield a
//
//  def withOutputStream (os: ⇒ OutputStream, msg: ⇒ String)
//    (f: OutputStream ⇒ ValLogIO[Unit]): ValLogIO[Unit] = withClose(os, msg)(f)
//
//  def withWriter (path: String, cs: CharSet)(f: Writer ⇒ ValLogIO[Unit])
//    : ValLogIO[Unit] = createFile(path) ∗ (withWriter(_, cs)(f))
//
//  def withWriter (file: ⇒ File, cs: CharSet)(f: Writer ⇒ ValLogIO[Unit])
//    : ValLogIO[Unit] = for {
//      fos ← fileOutputStream(file)
//      _   ← withWriter(fos, loc fileWriteError file, cs)(f)
//    } yield ()
//
//  def withWriter (os: ⇒ OutputStream, msg: ⇒ String, cs: CharSet)
//    (f: Writer ⇒ ValLogIO[Unit]): ValLogIO[Unit] =
//      success(new OutputStreamWriter(os, cs)) ∗ (withClose(_, msg)(f))
//
//  def copyBinary (from: String, to: String): ValLogIO[Unit] =
//    ^(file(from), createFile(to)) (copyBinary) join
//
//  def copyBinary (from: File, to: File): ValLogIO[Unit] = ^^(
//    fileInputStream(from),
//    fileOutputStream(to),
//    success(from.getPath)
//  ) ((i, o, p) ⇒ copyBinary(i, o, p)) join
//
//  def copyBinary (in: ⇒ InputStream, out: ⇒ OutputStream, nameExt: String)
//    : ValLogIO[Unit] = {
//    def msg = loc fileCopyError nameExt
//    lazy val ins = in
//    lazy val outs = out
//
//    def cp(i: InputStream, o: OutputStream): ValLogIO[Unit] = {
//      val bs = new Array[Byte](8192)
//      i read bs match {
//        case -1 ⇒ trace (loc fileCopied nameExt)
//        case x ⇒ { out.write(bs, 0, x); cp(i,o) }
//      }
//    }
//
//    withClose(ins, msg)(i ⇒ withClose(outs, msg)(o ⇒  cp(i,o)))
//  }
//
//  def resourceAsStream(nameExt: String, clazz: Class[_])
//    : ValLogIO[InputStream] = {
//    def stream = for {
//      is ← success (clazz getResourceAsStream nameExt)
//      _  ← trace (loc resourceOpened (nameExt, clazz))
//    } yield is
//
//    except (stream, loc resourceNotFound (nameExt, clazz, _))
//  }
//
//  import scala.xml.{XML, Node}
//
//  def writeString (s: String, path: String, cs: CharSet): ValLogIO[Unit] =
//    file(path) ∗ (f ⇒ writeString (s, f, cs))
//
//  def writeString (s: String, file: ⇒ File, cs: CharSet): ValLogIO[Unit] = for {
//    f  ← createFile(file)
//    os ← fileOutputStream(f)
//    _  ← writeString (s, os, loc fileWriteError file, cs)
//  } yield ()
//
//  def writeString (s: String, os: ⇒ OutputStream, msg: ⇒ String,
//    cs: CharSet): ValLogIO[Unit] = {
//    def write(w: Writer) =
//      success(w.write(s)) >> trace (loc stringWritten os.toString)
//  
//    withOutputStream (os, msg) (o ⇒ withWriter(o, msg, cs)(write))
//  }
//
//  def writeXml (n: Node, path: String, cs: CharSet): ValLogIO[Unit] =
//    file(path) ∗ (f ⇒ writeXml (n, f, cs))
//
//  def writeXml (n: Node, file: ⇒ File, cs: CharSet): ValLogIO[Unit] = for {
//    f  ← createFile(file)
//    os ← fileOutputStream(f)
//    _  ← writeXml (n, os, loc fileWriteError file, cs)
//  } yield ()
//
//  def writeXml (n: Node, os: ⇒ OutputStream, msg: ⇒ String, cs: CharSet)
//    : ValLogIO[Unit] = {
//    def write(w: Writer) = for {
//      _ ← success(XML.write(w, n, cs, true, null))
//      _ ← trace (loc xmlWritten os.toString)
//    } yield ()
//  
//    withOutputStream (os, msg) (o ⇒ withWriter(o, msg, cs)(write))
//  }
//
//  def readXml[T:ToXml](path: String): ValLogIO[T] =
//    file(path) ∗ (f ⇒ readXml[T](f))
//
//  def readXml[T:ToXml](file: ⇒ File): ValLogIO[T] =
//    fileInputStream(file) ∗ (fis ⇒ readXmlStream[T](fis))    
//
//  def readXmlStream[T:ToXml](in: ⇒ InputStream): ValLogIO[T] =
//    withInputStream (in, loc.dataReadError){ is ⇒ 
//      def readIO = liftDisIO(IO(XML.load(is).readD[T]))
//      trace (loc xmlRead in.toString) >> readIO
//    }
//}

// vim: set ts=2 sw=2 et:
