package efa.nb

import efa.core.ValRes
import efa.react.Out
import efa.react.swing.{GbPanel, failureColor}
import java.awt.Color
import scala.swing.{Label, Component}
import scalaz._, Scalaz._, effect._

class ValidatedPanel private (c: Component, invalidColor: Color)
extends GbPanel {
  import ValidatedPanel.empty

  private[this] val lbl = new Label(empty) {foreground = invalidColor}

  c fillV 1 above lbl add()
  
  def out[A]: Out[ValRes[A]] = vr ⇒ IO(
    vr fold (ss ⇒ lbl.text = ss.head, _ ⇒ lbl.text = empty)
  )
}

object ValidatedPanel {
  private val empty = " "

  def apply (c: Component, invalidColor: Color = failureColor)
    : IO[ValidatedPanel] = IO(new ValidatedPanel(c, invalidColor))
}

// vim: set ts=2 sw=2 et:
