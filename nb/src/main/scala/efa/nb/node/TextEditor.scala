package efa.nb.node

import java.awt.{Rectangle, Graphics}
import java.beans.PropertyEditorSupport
import org.openide.explorer.propertysheet.{ExPropertyEditor, InplaceEditor, PropertyEnv}
import scala.swing.{TextField, Label, Alignment}

class TextEditor(al: Alignment.Value) 
   extends PropertyEditorSupport
   with ExPropertyEditor
   with InplaceEditor.Factory{

  def attachEnv(env: PropertyEnv) { env.registerInplaceEditorFactory(this) }
  override def getInplaceEditor: InplaceEditor = new TextInplace
  override def isPaintable = true
  override def paintValue(g: Graphics, r: Rectangle): Unit = {
    val cbx = new Label(getValue.toString){
      horizontalAlignment = al
    }
    cbx.peer.setBounds(r)
    cbx.peer.paint(g)
  }
}

private [node] class TextInplace extends ComponentInplaceEditor[String] {
  protected val comp = new TextField
  override def get = comp.text
  override def set(o: String) { comp.text = o }
  override def supportsTextEntry = true
}

// vim: set ts=2 sw=2 et:
