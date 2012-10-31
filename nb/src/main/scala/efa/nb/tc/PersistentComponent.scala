package efa.nb.tc

import efa.core.Log, efa.core.Efa._
import efa.io._
import java.util.prefs.Preferences
import org.openide.util.NbPreferences
import scalaz._, Scalaz._, effect._

trait PersistentComponent extends ValLogIOFunctions with ValLogIOInstances {

  final def read: IO[Unit] = logger logValM {
    for {
      _ ← info ("Reading " + prefId)
      p ← prefs
      _ ← (p.get(prefId + "%s version", null) == version) ?
          readProps(p) |
          nullValLogIO
      _ ← liftIO (persistentChildren foldMap (_.read))
    } yield ()
  }

  protected def logger: LoggerIO = LoggerIO.consoleLogger

  protected def persistentChildren: List[PersistentComponent] = Nil 

  protected def prefId: String

  protected def version: String
  
  protected def prefs: ValLogIO[Preferences] = {
    val res = liftIO (IO (NbPreferences.forModule(getClass)))
    except (res, _ ⇒ "Unable to load preferences for " + getClass)
  }
  
  protected def readProps(prefs: Preferences): ValLogIO[Unit] = nullValLogIO
  
  protected def writeProps(prefs: Preferences): ValLogIO[Unit] = nullValLogIO
  
  final def persist: IO[Unit] = logger logValM {
    for {
      _ ← info ("Persisting " + prefId)
      p ← prefs
      _ ← liftIO (IO (p.put(prefId + "%s version", version)))
      _ ← writeProps(p)
      _ ← liftIO (persistentChildren foldMap (_.persist))
    } yield ()
  }
}

// vim: set ts=2 sw=2 et:
