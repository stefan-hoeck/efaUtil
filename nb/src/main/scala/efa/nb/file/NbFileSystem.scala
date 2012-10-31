package efa.nb.file

//import efa.core.Folder
//import efa.io.{StateIOs, StateIO, ValLogIO, ValLogIOs}
//import org.openide.filesystems.FileObject
//import scalaz._, Scalaz._, effects._
//
//case class NbFileSystem(id: Int, root: NbTree)
//
//object NbFileSystem extends StateIOs with ValLogIOs {
//  type IntState[A] = StateIO[Int,A]
//
//  def file (fo: FileObject): IntState[NbFile] = stateT (i ⇒ 
//    for {
//      name ← io(fo.getName)
//      ext  ← io(fo.getExt)
//    } yield (i + 1, NbFile(i, name, ext, fo))
//  )
//
//  def folder (fo: FileObject): IntState[NbFolder] = stateT (i ⇒ 
//    for {
//      name ← io(fo.getName)
//    } yield (i + 1, NbFolder(i, name, fo))
//  )
//
//  def toTree (fo: FileObject): IntState[NbTree] = for {
//    folder           ← NbFileSystem folder fo
//    children         ← fo.getChildren.toStream.η[IntState]
//    (folders, files) = children partition (_.isFolder)
//    nbFiles          ← files ↦ file
//    nbFolders        ← folders ↦ toTree
//  } yield Folder (nbFiles, nbFolders, folder)
//
//  def fromFo (fo: FileObject): IO[NbFileSystem] =
//    toTree(fo)(0) ∘ {case (i,r) ⇒ NbFileSystem(i,r)}
//
//  val id: Lens[NbFileSystem,Int] = Lens(_.id, (a,b) ⇒ a.copy(id = b))
//  val root: Lens[NbFileSystem,NbTree] = Lens(_.root, (a,b) ⇒ a.copy(root = b))
//  
//  implicit def nbFileSystemLenses[A] (l: Lens[A,NbFileSystem]) =
//    NbFileSystemLenses[A] (l)
//
//  case class NbFileSystemLenses[A] (l: Lens[A,NbFileSystem])  {
//
//    type StIO[T] = ValLogIO[State[A,T]]
//
//    lazy val id = l >>> NbFileSystem.id
//
//    lazy val root = l >>> NbFileSystem.root
//
//    def sortedFiles (a: A) = root.data(a) sortWith (_.name < _.name)
//
//    def deleteFile (f: NbFile): StIO[Unit] = for {
//      _ ← f.delete
//    } yield root remove f
//
//    def deleteFolder (f: NbTree): StIO[Unit] = for {
//      _ ← f.label.delete
//    } yield root removeFolder f
//
//    def renameFile (f: NbFile, name: String, ext: Option[String]): StIO[Unit] =
//      for {
//        _ ← f rename (name, ext)
//      } yield root update (f, f copy (name = name, ext = ext | f.ext))
//
//    def renameFolder (t: NbTree, name: String): StIO[Unit] = for {
//      _ ← t.label rename name
//    } yield root updateFolder (t, t.copy(label = t.label copy (name = name)))
//
//    def createFolder (t: NbTree, name: String): StIO[Unit] = for {
//      fo   ← t.label addFolder name
//    } yield for {
//              a     ← init[A]
//              newFo = fo copy (id = id(a))
//              _     ← id += 1
//              _     ← root addFolder (Folder leaf newFo, t)
//            } yield ()
//
//    def createFile (t: NbTree, name: String, ext: String): StIO[Unit] = for {
//      fi ← t.label addFile (name, ext)
//    } yield for {
//              a     ← init[A]
//              _     ← id += 1
//              _     ← root add(fi copy (id = id(a)), t)
//            } yield ()
//  }
//
//  implicit lazy val NbFileSystemEqual: Equal[NbFileSystem] = 
//    equalBy (f ⇒ (f.id, f.root))
//}

// vim: set ts=2 sw=2 et:
