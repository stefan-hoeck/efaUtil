package efa.nb.file

//import efa.core.{LoggerT, Folder, UniqueId}
//import efa.io.{ValLogIO, ValLogIOs, loc ⇒ ioLoc}, ValLogIOs._
//import efa.nb.loc
//import org.openide.filesystems.{FileObject, FileLock}
//import scalaz._, Scalaz._, effects._
//
//sealed trait NbFileObject {
//
//  def fo: FileObject
//
//  def id: Int
//
//  def name: String
//
//  protected def withLock[A] (
//    f: (FileObject, FileLock) ⇒ ValLogIO[A],
//    msg: Throwable ⇒ String
//  ): ValLogIO[A] = for {
//    lock ← lift (fo.lock)
//    a    ← except (
//             ensure(
//               f(fo, lock),
//               LoggerT.empty(try{lock.releaseLock()} catch {case e ⇒ })
//             ),
//             msg
//           )
//  } yield a
//
//  protected def withLockU[A] (
//    f: (FileObject, FileLock) ⇒ A,
//    msg: Throwable ⇒ String
//  ): ValLogIO[A] = withLock ((fo,l) ⇒ lift (f(fo,l)), msg)
//}
//
//case class NbFile private[file] (id: Int, name: String, ext: String, fo: FileObject)
//   extends NbFileObject {
//
//  lazy val nameExt = name + "." + ext
//
//  private[file] def delete: ValLogIO[Unit] = for {
//    _ ← withLockU (_.delete(_), ioLoc fileDeleteError (_, nameExt))
//    _ ← debug (ioLoc fileDeleted nameExt)
//  } yield ()
//
//  private[file] def rename (name: String, ext: Option[String]): ValLogIO[Unit] = for {
//    e ← lift (ext | this.ext)
//    n = name + "." + e
//    _ ← withLockU (_.rename(_, name, e), _ ⇒ loc fileRenameError nameExt)
//    _ ← debug (loc fileRenamed (nameExt, n))
//  } yield ()
//}
//
//case class NbFolder private[file] (id: Int, name: String, fo: FileObject)
//   extends NbFileObject {
//
//  private[file] def delete: ValLogIO[Unit] = for {
//    _ ← withLockU (_.delete(_), ioLoc folderDeleteError (_, name))
//    _ ← debug (ioLoc folderDeleted name)
//  } yield ()
//
//  private[file] def rename (name: String): ValLogIO[Unit] = for {
//    _ ← withLockU (_.rename(_, name, ""), _ ⇒ loc folderRenameError this.name)
//    _ ← debug (loc folderRenamed (this.name, name))
//  } yield ()
//
//  private[file] def addFile (name: String, ext: String): ValLogIO[NbFile] = {
//    val ne = name + "." + ext
//    for {
//      f ← withLockU((f,_) ⇒ f.createData(name, ext),
//                    ioLoc.fileCreateError(_, ne))
//      _ ← debug (ioLoc fileCreated ne)
//    } yield new NbFile(0, name, ext, f)
//  }
//
//  private[file] def addFolder (name: String): ValLogIO[NbFolder] = for {
//    f ← withLockU((f,_) ⇒ f.createFolder(name),
//                  ioLoc.folderCreateError(_, name))
//    _ ← debug (ioLoc folderCreated name)
//  } yield new NbFolder(0, name, f)
//}
//
//object NbFileObject {
//  implicit val NbFileObjectEqual = new Equal[NbFileObject] {
//    def equal (a: NbFileObject, b: NbFileObject) = (a,b) match {
//      case (NbFile(a, b, c, d), NbFile(e, f, g, h)) ⇒
//        (a, b, c, d) == (e, f, g, h)
//
//      case (NbFolder(a, b, c), NbFolder(e, f, g)) ⇒
//        (a, b, c) == (e, f, g)
//
//      case _ ⇒ false
//    }
//  }
//
//  implicit val NbFileObjectUniqueId = UniqueId get {f: NbFileObject ⇒ f.id}
//
//}

// vim: set ts=2 sw=2 et:
