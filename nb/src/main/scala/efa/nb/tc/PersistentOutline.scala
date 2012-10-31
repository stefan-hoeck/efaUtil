package efa.nb.tc

import java.util.Properties
import java.util.prefs.Preferences
import org.openide.explorer.view.OutlineView
import scala.collection.JavaConversions._
import scalaz._, Scalaz._, effect._

trait PersistentOutline extends PersistentComponent {
  import WithOutline._
  protected def outline: OutlineView

  final protected def rowHeight = IO (outline.getOutline.getRowHeight)

  final protected def rowHeightSet (rh: Int) =
    IO (outline.getOutline.setRowHeight(rh))

  final protected def enlarge: IO[Unit] =
    rowHeight >>= (rh ⇒ rowHeightSet((rh + Step) min MaxRowHeight))

  final protected def reduce: IO[Unit] =
    rowHeight >>= (rh ⇒ rowHeightSet((rh - Step) max MaxRowHeight))
  
  private[this] lazy val prefix = prefId + WithOutline.OutlineView
  
  override protected def writeProps(prefs: Preferences) = for {
    rh ← liftIO (rowHeight)
    _  ← point (prefs.putInt(prefId + RowHeight, rh))
    ps ← point (new Properties)
    _  ← point (outline.writeSettings(ps, prefix))
    _  ← ps.stringPropertyNames.toList filter (_ startsWith prefix) foldMap (
           k ⇒ point (prefs.put(k, ps.getProperty(k)))
         )
  } yield ()

  override protected def readProps(prefs: Preferences) = for {
    rh ← point (prefs.getInt(prefId + RowHeight, MinRowHeight))
    ps ← point (new Properties)
    _  ← prefs.keys.toList filter (_ startsWith prefix) foldMap {
           k ⇒ point (ps.put(k, prefs.get(k, ""))) >| ()
         }
    _  ← try {
           outline.readSettings(ps, prefix)
           nullValLogIO 
         } catch {
           //do nothing, happens when no settings where stored
           case e: NullPointerException ⇒ nullValLogIO
           case e: Exception ⇒  warning (readError(e))
         }
  } yield ()

  private def readError (e: Exception) = 
    "Error when reading props for %s: %s" format (prefId, e.toString)
}

// vim: set ts=2 sw=2 et:
