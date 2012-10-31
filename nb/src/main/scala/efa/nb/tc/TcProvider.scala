package efa.nb.tc

import efa.io.{LoggerIO, IOCached}
import org.openide.windows.{TopComponent, WindowManager}
import scalaz._, Scalaz._, effect._

abstract class TcProvider[Tc <: TopComponent](
  factory: IO[Tc], logger: LoggerIO
)
(implicit m: Manifest[Tc]) {
  import TcProvider._

  private[this] val inst = IOCached(factory)
  private[this] val tcc = m.erasure

  protected val preferredId: String
  
  private[this] def getDef: IO[Tc] =
    logger.debug ("Get default incstance for " + preferredId) >>
    inst.get

  private[this] def findInst: IO[Tc] = for {
    _  ← logger.debug ("Searching instance for " + preferredId)
    tc ← IO(WindowManager.getDefault().findTopComponent(preferredId))
    r  ← tc match {
           case null ⇒  logger.warn(notFound(preferredId)) >> getDef
           case w if(tcc isAssignableFrom w.getClass) ⇒ IO(w.asInstanceOf[Tc])
           case _ ⇒ logger.warn(multipleFound(preferredId)) >> getDef
         }
  } yield r

  /**
   * Gets default instance. Do not use directly:
   * reserved for *.settings files only,
   * i.e. deserialization routines; otherwise you could
   * get a non-deserialized instance.
   * To obtain the singleton instance, use {@link #findInstance}.
   */
  def getDefault: Tc = getDef.unsafePerformIO

  /**
   * Obtain the TopComponent instance.
   * Never call {@link #getDefault} directly!
   */
  def findInstance: Tc = findInst.unsafePerformIO
}

object TcProvider {
  private def notFound (id: String) = 
    "Cannot find " + id + 
    " component. It will not be located properly in the window system."

  private def multipleFound (id: String) = 
    "There seem to be multiple components with the '" + id +
    "' ID. That is a potential source of errors and unexpected behavior."
}

// vim: set ts=2 sw=2 et:
