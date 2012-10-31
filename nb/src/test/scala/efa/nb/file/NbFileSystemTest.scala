package efa.nb.file
//
//import efa.core.Level
//import efa.io.{LoggerIO, ValLogIO, ValLogIOs}
//import org.scalacheck._, Prop._
//import scala.collection.JavaConversions._
//import scalaz._, Scalaz._, effects._
//
//object NbFileSystemTest extends Properties("NbFileSystem") with ValLogIOs {
//  import Generators._
//
//  val logger = LoggerIO.consoleLogger filter Level.Error
//
//  def runProp (p: ValLogIO[Prop]) = logger.logVal(p, false: Prop).unsafePerformIO
//
//  property("fromFo") = Prop.forAll {fs: NbFileSystem ⇒ 
//    val dataIds = fs.root.allData ∘ (_.id) toList
//    val folderIds = fs.root.allFolders ∘ (_.label.id) toList
//    val allIds = (dataIds ::: folderIds) sortWith (_ < _)
//    val length = allIds.length
//    val exp = List.range(0, length)
//
//    (allIds ≟ exp) && (fs.id ≟ length)
//  }
//
//  val selfL = Lens.self[NbFileSystem]
//
//  property("deleteFile") = Prop.forAll {fs: NbFileSystem ⇒
//    val goner = fs.root.data.head
//    
//    val res = for  {
//      st  ← selfL deleteFile goner
//      nfs = st ~> fs 
//    } yield (nfs.root.find(goner ≟) ≟ none) :| "fs updated" &&
//            !hasFile(nfs, goner.name) :| "deleted"
//
//    runProp(res)
//  }
//
//  property("deleteFolder") = Prop.forAll {fs: NbFileSystem ⇒
//    val goner = fs.root.folders.head
//    
//    val res = for  {
//      st  ← selfL deleteFolder goner
//      nfs = st ~> fs 
//    } yield (nfs.root.findFolder(goner ≟) ≟ none) :| "fs updated" &&
//            !hasFolder(nfs, goner.label.name) :| "deleted"
//
//    runProp(res)
//  }
//
//  property("renameFile") = Prop.forAll {fs: NbFileSystem ⇒
//    val file = fs.root.data.head
//    val name = (fs.root.data map (_.name) mkString "") + "a"
//    
//    val res = for  {
//      st  ← selfL renameFile (file, name, none)
//      nfs = st ~> fs 
//    } yield (nfs.root.find(_.name ≟ name).nonEmpty) :| "fs updated" &&
//            hasFile(nfs, name) :| "updated"
//
//    runProp(res)
//  }
//
//  property("renameFolder") = Prop.forAll {fs: NbFileSystem ⇒
//    val folder = fs.root.folders.head
//    val name = (fs.root.folders map (_.label.name) mkString "") + "a"
//    
//    val res = for  {
//      st  ← selfL renameFolder (folder, name)
//      nfs = st ~> fs 
//    } yield (nfs.root.folders.find(_.label.name ≟ name).nonEmpty) :| "fs updated" &&
//            hasFolder(nfs, name) :| "updated"
//
//    runProp(res)
//  }
//
//  property("createFile") = Prop.forAll {fs: NbFileSystem ⇒
//    val name = (fs.root.data map (_.name) mkString "") + "a"
//    
//    val res = for  {
//      st  ← selfL createFile (fs.root, name, "data")
//      nfs = st ~> fs 
//    } yield (nfs.root.find(_.name ≟ name).nonEmpty) :| "fs updated" &&
//            hasFile(nfs, name) :| "updated" &&
//            (nfs.id ≟ (fs.id + 1)) :| "FS id" &&
//            (nfs.root.find(_.name ≟ name).get.id ≟ fs.id) :| "File id"
//
//
//    runProp(res)
//  }
//
//  property("createFolder") = Prop.forAll {fs: NbFileSystem ⇒
//    val name = (fs.root.folders map (_.label.name) mkString "") + "a"
//    
//    val res = for  {
//      st  ← selfL createFolder (fs.root, name)
//      nfs = st ~> fs 
//    } yield (nfs.root.findFolder(_.label.name ≟ name).nonEmpty) :| "fs updated" &&
//            hasFolder(nfs, name) :| "updated" &&
//            (nfs.id ≟ (fs.id + 1)) :| "FS id" &&
//            (nfs.root.findFolder(_.label.name ≟ name).get.label.id ≟ fs.id) :| "Folder id"
//
//
//    runProp(res)
//  }
//
//
//  private def hasFile (fs: NbFileSystem, ne: String) =
//    fs.root.label.fo.getChildren find (_.getName ≟ ne) nonEmpty
//
//  private def hasFolder (fs: NbFileSystem, name: String) =
//    fs.root.label.fo.getChildren find (_.getNameExt ≟ name) nonEmpty
//}
//
//// vim: set ts=2 sw=2 et:
