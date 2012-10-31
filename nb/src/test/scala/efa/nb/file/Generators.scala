package efa.nb.file
//
//import efa.core.Folder
//import org.openide.filesystems.{FileObject, FileUtil}
//import org.scalacheck._, Prop._
//import scalaz._, Scalaz._, scalacheck.ScalaCheckBinding._
//
//object Generators {
//
//  type Fld = Folder[String, String]
//
//  def streamGen[A] (g: Gen[A]) = for {
//    c ← Gen choose (1, 3)
//    as ← Gen listOfN (c, g)
//  } yield as.toStream
//
//  val lblGen = Gen.identifier
//  val dataGen = Gen.identifier
//  val datasGen = streamGen(dataGen) ∘ (_.distinct)
//  def leafGen (lbl: String) = datasGen ∘ (Folder(_, Stream.empty, lbl))
//  val leafsGen = datasGen >>= (_ ↦ leafGen)
//  def l1Gen(lbl: String) = datasGen ⊛ leafsGen apply (Folder(_, _, lbl))
//  val l1sGen = datasGen >>= (_ ↦ l1Gen)
//  val rootGen: Gen[Fld] = datasGen ⊛ l1sGen ⊛ lblGen apply Folder.apply
//
//  private def adjustFo (fo: FileObject, folder: Fld) {
//    folder.data foreach {fo.createData(_, "data")}
//    folder.folders foreach {f ⇒ 
//      val newFo = fo.createFolder(f.label)
//      adjustFo(newFo, f)
//    }
//  }
//
//  private def folderToFo (f: Fld): FileObject = {
//    val fo = FileUtil.createMemoryFileSystem.getRoot.createFolder(f.label)
//    adjustFo(fo, f)
//    fo
//  }
//
//  val foGen: Gen[FileObject] = rootGen map folderToFo
//
//  val fsGen: Gen[NbFileSystem] = foGen ∘ (fo ⇒ NbFileSystem fromFo fo unsafePerformIO)
//
//  implicit lazy val fsArbitrary = Arbitrary(fsGen)
//}
//
//// vim: set ts=2 sw=2 et:
