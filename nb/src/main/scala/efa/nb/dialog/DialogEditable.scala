package efa.nb.dialog

import efa.core._
import efa.nb.{VSIn, ValidatedPanel, loc}
import efa.react.{Out, EET, eTrans}
import scala.swing.event.ActionEvent
import javax.swing.JDialog
import org.openide.{DialogDisplayer, DialogDescriptor}
import scala.swing.{Component, Button}
import scalaz._, Scalaz._, effect.IO

trait DialogEditable[-A,+B] {
  import efa.react.swing.abstractButton._
  import DialogEditable._

  type Comp <: Component

  def component (a: A): IO[Comp]

  def signalIn (c: Comp): VSIn[B]

  def name (a: A): String = a.toString

  def editTitle (a: A): String = loc editTitle name(a)

  def newTitle (a: A): String = loc newTitle name(a)

  final def editDialog (title: String, a: A): IO[Option[B]] = for {
    p   ← component(a)
    ref ← IO newIORef "".failureNel[B]
    vp  ← ValidatedPanel(p)
    d   ← dialog(vp, title)
    btn ← button[B](d)
    p   ← signalIn(p) to (btn.out ⊹ vp.out) on btn to (ref write _) apply ()
    _   ← IO(d.setVisible(true))
    res ← ref.read
    _   ← p._1.toList foldMap (_.disconnect) //clean up events
  } yield res.toOption

  final def edit (a: A): IO[Option[B]] = editDialog(editTitle(a), a)

  final def create (a: A): IO[Option[B]] = editDialog(newTitle(a), a)

  lazy val editTrans: EET[A,B] = eTrans.id collectIO edit

//  def ntInfo (a: A, out: Out[B]): NtInfo = (name(a), edit(a, out))
}

object DialogEditable {

  def apply[A:Show,B,C <: Component](c: A ⇒ C)(in: C ⇒ VSIn[B])
    : DialogEditable[A,B] = io[A,B,C](a ⇒ (IO(c(a))))(in)

  def io[A:Show,B,C <: Component](c: A ⇒ IO[C])(in: C ⇒ VSIn[B])
    : DialogEditable[A,B] = new DialogEditable[A,B] {
    type Comp = C
    def component (a: A) = c(a)
    def signalIn (c: C) = in(c)
    override def name (a: A) = Show[A] shows a
  }

  private def button[B](d: JDialog) = IO(
    new Button {
      override lazy val peer = d.getRootPane.getDefaultButton
      lazy val out: Out[ValRes[B]] = vr ⇒ IO(enabled = vr.isSuccess)
    }
  )

  private def dialog (c: Component, title: String): IO[JDialog] = IO (
    DialogDisplayer.getDefault.createDialog(
      new DialogDescriptor(c.peer, title, true, null)
    ).asInstanceOf[JDialog]
  )
}

// vim: set ts=2 sw=2 et:
