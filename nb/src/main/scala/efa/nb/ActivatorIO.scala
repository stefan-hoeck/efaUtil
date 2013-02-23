package efa.nb

import org.osgi.framework.{BundleActivator, BundleContext}
import scalaz.effect.IO

trait ActivatorIO extends BundleActivator {
  final def start(c: BundleContext) { startIO(c).unsafePerformIO() }

  final def stop(c: BundleContext) { stopIO(c).unsafePerformIO() }

  protected def startIO(c: BundleContext): IO[Unit] = startIO

  protected def stopIO(c: BundleContext): IO[Unit] = stopIO

  protected def startIO: IO[Unit] = IO.ioUnit

  protected def stopIO: IO[Unit] = IO.ioUnit
}

// vim: set ts=2 sw=2 et:
