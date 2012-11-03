package efa.nb.node

import scalaz._, Scalaz._, effect.{IO, IORef}
import efa.core._, Efa._
import efa.nb.PureLookup
import efa.nb.dialog.DialogEditable
import efa.react._
import java.beans.PropertyEditor
import org.openide.nodes.{Children, AbstractNode, Sheet, Node, NodeTransfer}

object NbNode {
  import NodeOut.{outOnly, outImpure}

  def apply: IO[NbNode] = for {
    hc  ← NbChildren.create
    lkp ← PureLookup.apply
    res ← IO (new NbNode (lkp, hc))
  } yield res

  def create (ns: NodeSetter): IO[NbNode] = for {
    res ← apply
    _   ← ns setNode res
  } yield res

  private[node] def createIO(ns: NodeSetter) =
    create(ns).unsafePerformIO

  val contextRoots: NodeOut[List[String],Nothing] = outOnly(_.contextRoots)

  def contextRootsA[A] (ss: List[String]): NodeOut[A,Nothing] =
    contextRoots ∙ (_ ⇒ ss)

  def cookie[A:Manifest]: NodeOut[A,Nothing] = cookies[A] ∙ (List(_))

  def cookies[A:Manifest]: NodeOut[List[A],Nothing] =
    outOnly(_.updateCookies[A])

  def cookieOption[A:Manifest]: NodeOut[Option[A],Nothing] =
    cookies[A] ∙ (_.toList)

  def desc[A] (desc: A ⇒ String): NodeOut[A,Nothing] =
    outImpure((n,a) ⇒ n.setShortDescription(desc(a)))

  def destroy[A]: NodeOut[A,A] = destroyOption ∙ (_.some)

  def destroyEs[A,B] (f: A ⇒ State[B,Unit]): NodeOut[A,ValSt[B]] =
    destroy[A] map (f(_).success)

  def destroyOption[A]: NodeOut[Option[A],A] =
    NodeOut((outA, n) ⇒ oa ⇒ n setDestroyer oa.map(outA))

  def edit[A]: NodeOut[A,A] = editOption ∙ (_.some)

  def editOption[A]: NodeOut[Option[A],A] =
    NodeOut((outA, n) ⇒ oa ⇒ n setEditor oa.map(outA))

  def editDialog[A,B](implicit D: DialogEditable[A,B]): NodeOut[A,B] =
    edit[A] collectIO D.edit

  def editDialogEs[A,B,C] (f: B ⇒ State[C,Unit])
    (implicit D: DialogEditable[A,B]): NodeOut[A,ValSt[C]] =
    editDialog[A,B] map (f(_).success)

  val iconBase: NodeOut[String,Nothing] =
    outImpure(_ setIconBaseWithExtension _)

  def iconBaseA[A] (s: String): NodeOut[A,Nothing] = iconBase ∙ (_ ⇒ s)

  def name[A] (f: A ⇒ String): NodeOut[A, Nothing] =
    outImpure((n,a) ⇒ n.setDisplayName(f(a)))

  def nameA[A] (s: String): NodeOut[A, Nothing] = name(_ ⇒ s)

//  def property[A:Manifest] (
//    name: String, editor: Option[A ⇒ PropertyEditor] = None
//  ): NodeOut[A] = (n,a) ⇒ RProp[A] (name, a, editor) ∗ n.setPut
//
//  def propertyRw[A:Manifest] (
//    name: String,
//    src: EventSource[ValRes[A]],
//    validator: EndoVal[A] = Validators.dummy[A],
//    editor: Option[A ⇒ PropertyEditor] = None
//  ): NodeOut[A] =
//    (n,a) ⇒ RwProp[A] (name, a, editor, src, validator) ∗ n.setPut

  val rename: NodeOut[Any,String] = NodeOut((o, n) ⇒ _ ⇒ n onRename o)

  lazy val renameD: NodeOut[EndoVal[String],DisRes[String]] =
    rename withIn (_ run _)

  lazy val renameV: NodeOut[EndoVal[String],ValRes[String]] =
   renameD map (_.validation)

  def renameEs[A,B] (f: (A,String) ⇒ State[B,Unit])
  : NodeOut[(A,EndoVal[String]),ValSt[B]] = {
    type P = (A,EndoVal[String])
    renameV.contramap[P] (_._2) withIn ((p,v) ⇒ v map (f(p._1, _)))
  }

  def addNt[A]: NodeOut[(A,String),A] =
    NodeOut((o, n) ⇒ p ⇒ n addNewType (p._2, o apply p._1))

  def addNtDialog[A,B](implicit D: DialogEditable[A,B])
    : NodeOut[A,B] =
    addNt[A] contramap {a: A ⇒ (a, D name a)} collectIO D.create

  def addNtDialogEs[A,B,C] (f: B ⇒ State[C,Unit])
    (implicit D: DialogEditable[A,B])
    : NodeOut[A,ValSt[C]] = addNtDialog map (f(_).success)

  /**
   * All NodeOuts defined for adding new types where defined
   * will modify the existing list of new type infos. That way,
   * they are composable via monoid append.
   *
   * However, if several such modifications affect the same node,
   * The node's list of NewTypes will grow and grow. Therefore,
   * add this NodeOut before all addNt kind of NodeOuts, so the
   * list of NewTypes will be cleared before new ones are added.
   */
  lazy val clearNt: NodeOut[Any,Nothing] =
    NodeOut((_, n) ⇒ _ ⇒ n setNewTypes Nil)

  //// Private Helper classes

  private class RProp[A] private (
    override val getName: String,
    override val getValue: A,
    editor: Option[A ⇒ PropertyEditor]
  )(implicit m: Manifest[A])
   extends Node.Property[A] (m.erasure.asInstanceOf[Class[A]]) {
    override def canRead = true
    override def canWrite = false
    override def setValue (a: A) {}
    override def getPropertyEditor =
      editor ∘ (_ apply getValue) | super.getPropertyEditor
  }

  private object RProp {
    def apply[A:Manifest] (
      name: String, a: A, editor: Option[A ⇒ PropertyEditor]
    ): IO[RProp[A]] = IO { new RProp (name, a, editor) }
  }

  private class RwProp[A] (
    override val getName: String,
    override val getValue: A,
    editor: Option[A ⇒ PropertyEditor],
    out: Out[ValRes[A]],
    validator: EndoVal[A]
  )(implicit m: Manifest[A])
  extends Node.Property[A](m.erasure.asInstanceOf[Class[A]]) {
    override def canRead = true
    override def canWrite = true

    override def setValue (a: A) {
      out apply validator(a).validation unsafePerformIO
    }

    override def getPropertyEditor =
      editor map (_ apply getValue) getOrElse super.getPropertyEditor
  }

  private object RwProp {
    def apply[A:Manifest] (
      name: String,
      a: A,
      editor: Option[A ⇒ PropertyEditor],
      out: Out[ValRes[A]],
      validator: EndoVal[A]
    ): IO[RwProp[A]] = IO { new RwProp (name, a, editor, out, validator) }
  }
}

final class NbNode private (
  lkp: PureLookup,
  private[node] val hc: NbChildren
) extends PureNode (hc, lkp.l) with ContextActionNode {

  // *** Cookies ***
  
  def updateCookies[A:Manifest]: Out[List[A]] = lkp.set

  // *** Property Sheet ***
  private[this] lazy val set = Sheet.createPropertiesSet

  def setPut:  Out[org.openide.nodes.Node.Property[_]] = p ⇒ IO(set put p)
  
  override final protected def createSheet: Sheet = {
    val sheet = Sheet.createDefault
    sheet put set
    sheet
  }
  
  // *** Edit ***
  def setEditor: Out[Editor] = { e ⇒ 
    import org.openide.cookies.EditCookie

    def ec = (io: IO[Unit]) ⇒ new EditCookie {
      def edit() { io.unsafePerformIO }
    }

    updateCookies[EditCookie] apply (e map ec toList)
  }

  def onEdit: Out[IO[Unit]] = setEditor ∙ (_.some)

  override final def canCopy = true

  override final def canCut = true
}

// vim: set ts=2 sw=2 et:
