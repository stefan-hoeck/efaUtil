package efa.nb.node

import scalaz._, Scalaz._, effect.{IO, IORef}
import efa.core._, Efa._
import efa.nb.PureLookup
import efa.nb.dialog.DialogEditable
import efa.react._
import java.beans.PropertyEditor
import org.openide.nodes.{Children, AbstractNode, Sheet, Node, NodeTransfer}
import scala.swing.Alignment

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

  def booleanProp(n: String): NodeOut[Boolean,Nothing] =
    writeProp[Boolean,Boolean](n, identity, Some(_ ⇒ new BooleanEditor))

  def intProp(n: String): NodeOut[Int,Nothing] =
    textProp[Int,Int](n, identity, al = Alignment.Trailing)

  def longProp(n: String): NodeOut[Long,Nothing] =
    textProp[Long,Long](n, identity, al = Alignment.Trailing)

  def doubleProp(n: String, format: Double ⇒ String)
  : NodeOut[Double,Nothing] = textProp[Double,Double](
    n, identity, toString = format, al = Alignment.Trailing
  )

  def textProp[A,B:Manifest](
    name: String,
    toB: A ⇒ B,
    toString: A ⇒ String = (a: A) ⇒ a.toString,
    desc: A ⇒ Option[String] = (a: A) ⇒ None,
    al: Alignment.Value = Alignment.Leading
  ): NodeOut[A,Nothing] =
    writeProp[A,B](name, toB, TextEditor.read(al, toString, desc))

  def booleanRwProp (n: String): NodeOut[Boolean,ValRes[Boolean]] =
    rwProp[Boolean,Boolean](
      n, identity, Validators.dummy, Some((_,_) ⇒ new BooleanEditor)
    )

  def booleanRwPropSetGet[A,B](set: (A,Boolean) ⇒ State[B,Unit])
    (get: A ⇒ Boolean, n: String): NodeOut[A,ValSt[B]] =
    booleanRwProp(n) contramap get withIn ((a,vb) ⇒ vb map (set(a, _)))

  def comboRwProp[A:Manifest] (
    as: List[A],
    n: String,
    al: Alignment.Value = Alignment.Trailing
  ): NodeOut[A,ValRes[A]] =
    rwProp[A,A](
      n, identity, Validators.dummy, Some((_,_) ⇒ new ComboBoxEditor(as, al))
    )

  def comboRwPropSetGet[A,B,C:Manifest](set: (A,C) ⇒ State[B,Unit])(
    get: A ⇒ C,
    cs: List[C],
    n: String,
    al: Alignment.Value = Alignment.Trailing
  ): NodeOut[A,ValSt[B]] =
    comboRwProp(cs, n, al) contramap get withIn ((a,vc) ⇒ vc map (set(a, _)))

  def intRwProp (n: String, v: EndoVal[Int]): NodeOut[Int,ValRes[Int]] =
   readRwProp[Int](n, v, al = Alignment.Trailing)

  def longRwProp (n: String, v: EndoVal[Long]): NodeOut[Long,ValRes[Long]] =
   readRwProp[Long](n, v, al = Alignment.Trailing)

  def stringRwProp (n: String, v: EndoVal[String])
    : NodeOut[String,ValRes[String]] =
   readRwProp[String](n, v, al = Alignment.Leading)

  def readRwProp[A:Read:Manifest](
    name: String,
    validator: EndoVal[A],
    toString: A ⇒ String = (a: A) ⇒ a.toString,
    desc: A ⇒ Option[String] = (a: A) ⇒ None,
    al: Alignment.Value = Alignment.Leading
  ): NodeOut[A,ValRes[A]] = textRwProp[A](
    name, Read[A].validator >=> validator, toString, desc, al
  )

  def textRwProp[A:Manifest](
    name: String,
    read: Validator[String,A],
    toString: A ⇒ String = (a: A) ⇒ a.toString,
    desc: A ⇒ Option[String] = (a: A) ⇒ None,
    al: Alignment.Value = Alignment.Leading
  ): NodeOut[A,ValRes[A]] = {
    def ed (a: A, o: Out[ValRes[A]]) = {
      val textOut: Out[String] = o ∙ (read run _ validation)
      
      TextEditor.rw(a, al, toString, desc, textOut)
    }

    rwProp[A,A](name, identity, Validators.dummy, Some(ed(_, _)))
  }

  def rwProp[A,B:Manifest](
    name: String,
    toB: A ⇒ B,
    validator: EndoVal[B],
    editor: Option[(A, Out[ValRes[B]]) ⇒ PropertyEditor]
  ): NodeOut[A,ValRes[B]] =
    NodeOut (
    (o, n) ⇒ a ⇒ RwProp[A,B](name, a, toB, validator, editor, o) >>= n.setPut
  )

  def writeProp[A,B:Manifest](
    name: String,
    toB: A ⇒ B,
    editor: Option[A ⇒ PropertyEditor]
  ): NodeOut[A,Nothing] =
    NodeOut (
    (_, n) ⇒ a ⇒ RProp[A,B](name, a, toB, editor) >>= n.setPut
  )

  //// Private Helper classes

  private class RProp[A,B] private (
    override val getName: String,
    a: A,
    toB: A ⇒ B,
    editor: Option[A ⇒ PropertyEditor]
  )(implicit m: Manifest[B])
   extends Node.Property[B] (m.erasure.asInstanceOf[Class[B]]) {
    override def canRead = true
    override def canWrite = false
    override def setValue (a: B) {}
    override def getValue: B = toB(a)
    override def getPropertyEditor =
      editor ∘ (_ apply a) | super.getPropertyEditor
  }

  private object RProp {
    def apply[A,B:Manifest] (
      name: String,
      a: A,
      toB: A ⇒ B,
      editor: Option[A ⇒ PropertyEditor]
    ): IO[RProp[A,B]] = IO { new RProp (name, a, toB, editor) }
  }

  private class RwProp[A,B] (
    override val getName: String,
    a: A,
    toB: A ⇒ B,
    validator: EndoVal[B],
    editor: Option[(A, Out[ValRes[B]]) ⇒ PropertyEditor],
    out: Out[ValRes[B]]
  )(implicit m: Manifest[B])
   extends Node.Property[B](m.erasure.asInstanceOf[Class[B]]) {
    override def canRead = true
    override def canWrite = true
    override def getValue: B = toB (a)

    override def setValue (b: B) {
      out apply validator(b).validation unsafePerformIO
    }

    override def getPropertyEditor =
      editor ∘ (_ apply (a, out)) | super.getPropertyEditor
  }

  private object RwProp {
    def apply[A,B:Manifest] (
      name: String,
      a: A,
      toB: A ⇒ B,
      validator: EndoVal[B],
      editor: Option[(A, Out[ValRes[B]]) ⇒ PropertyEditor],
      out: Out[ValRes[B]]
    ): IO[RwProp[A,B]] = IO { new RwProp (name, a, toB, validator, editor, out) }
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
