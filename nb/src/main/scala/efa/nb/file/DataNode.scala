package efa.nb.file

//import efa.core.Validator
//import efa.io.{LoggerIO, ValLogIO, ValLogIOs}
//import efa.nb.loc
//import efa.nb.node.{NodeOut,NbNode, NbChildren, NtInfo}, NbChildren._
//import efa.react.{EventSource, Out}
//import org.openide.filesystems.FileObject
//import scalaz._, Scalaz._, effects._
//
//case class NbTreeNodes (logger: LoggerIO) {
//  import ValLogIOs._, DataNode._
//
//  private val lens = Lens.self[NbFileSystem]
//
//  def fileName: NodeOut[NbFile] = NbNode name (_.name)
//
//  def folderName: NodeOut[NbTree] = NbNode name (_.label.name)
//
//  def deleteFile (es: EsStU): NodeOut[NbFile] =
//    NbNode destroy (f ⇒ withEs(es, lens deleteFile f))
//
//  def deleteFolder (es: EsStU): NodeOut[NbTree] =
//    NbNode destroy (f ⇒ withEs(es, lens deleteFolder f))
//
//  def renameFile (es: EsStU): NodeOut[NbFile] =
//    (n, f) ⇒ n onRename (s ⇒ withEs(es, lens renameFile (f, s, None)))
//
//  def renameFolder (es: EsStU): NodeOut[NbTree] =
//    (n, f) ⇒ n onRename (s ⇒ withEs(es, lens renameFolder (f, s)))
//
//  def fileFactory (out: NodeOut[NbFile]): Factory[NbTree] =
//    uniqueIdF[NbFile,Int] (out) ∙ (_.data sortBy (_.name))
//
//  def folderFactory[A] (out: NodeOut[NbTree]): Factory[NbTree] =
//    uniqueIdF[NbTree,Int] (out) ∙ (_.folders sortBy (_.label.name))
//
//  def newFolder(p: NbTree, es: EsStU): NtInfo = {
//    def out = (s: String) ⇒ withEs(es, lens createFolder (p, s))
//
//    NamePanelFactory.default ntInfo (loc.folder, out)
//  }
//
//  def defaultOut (
//    fileOut: NodeOut[NbFile],
//    newTypes: NbTree ⇒ Seq[NtInfo],
//    contextRoots: Boolean ⇒ Seq[String],
//    es: EsStU
//  ): NodeOut[NbTree] = {
//    lazy val totalFileOut =
//      fileName ⊹ deleteFile(es) ⊹ renameFile(es) ⊹ fileOut
//
//    def contextRoot (b: Boolean): NodeOut[NbTree] =
//      NbNode contextRoots (_ ⇒ contextRoots(b))
//
//    // NewTypes
//    lazy val ntOut: NodeOut[NbTree] =
//      (n,t) ⇒ n setNewTypes (newFolder(t, es) +: newTypes(t))
//
//    // Folders
//    lazy val fFactory: Factory[NbTree] = folderFactory (allOut)
//
//    // Files
//    lazy val chldOut: NodeOut[NbTree] =
//      children(fFactory, fileFactory (totalFileOut))
//
//    lazy val allOut: NodeOut[NbTree] =
//      folderName ⊹ deleteFolder (es) ⊹ renameFolder (es) ⊹
//      chldOut ⊹ ntOut ⊹ contextRoot(false)
//
//    folderName ⊹ chldOut ⊹  ntOut ⊹ contextRoot(true)
//  }
//
//
//  protected def withEs(es: EsStU, f: StIOU): IO[Unit] =
//    (logger logValZ f) >>= es.fire
//}
//
//object DataNode {
//
//  type StateTrans = State[NbFileSystem,Unit]
//  type EsStU = EventSource[StateTrans]
//  type StIOU = ValLogIO[StateTrans]
//
//  /**
//   * Node controller for NbFileSystem
//   *
//   * @param root     The root file object
//   * @param out      
//   */
//  def nodeFor (root: FileObject, nodeOut: EsStU ⇒ NodeOut[NbTree]): IO[NbNode] =
//    for {
//      states        ← EventSource.create[StateTrans]
//      initial       ← NbFileSystem fromFo root
//      inputs        ← states.fold[NbFileSystem](initial, _ ~> _)
//
//      res           ← NbNode create (inputs map (t ⇒ nodeOut(states)(_, t.root)))
//    } yield res
//}

// vim: set ts=2 sw=2 et:
