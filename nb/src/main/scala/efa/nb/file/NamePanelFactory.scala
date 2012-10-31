package efa.nb.file

//import efa.core.{Reads, loc ⇒ coreLoc, Validator, Validators}
//import efa.nb.dialog.EditPanelFactory
//import scalaz.effects.io
//
//case class NamePanelFactory(validator: Validator[String,String])
//   extends EditPanelFactory[String,String] with Reads {
//  type EditPanel = FPanel
//
//  protected def create (s: String) = io (new FPanel(s))
//
//  protected def events (p: FPanel) = text[String] (p.nameC, validator)
//
//  class FPanel private[NamePanelFactory] (s: String) extends MyPanel {
//    val nameC = textField (s)
//    def elems = coreLoc.name beside nameC
//  }
//
//  override def name (s: String) = s
//
//  override protected def preferredSize (w: Int, h: Int) = (400, h)
//}
//
//object NamePanelFactory {
//  lazy val default = NamePanelFactory(Validators.notEmptyString)
//}

// vim: set ts=2 sw=2 et:
