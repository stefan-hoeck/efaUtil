package efa.io

import efa.core._, Default._
import scalaz._, Scalaz._, effect._

sealed trait LoggerIO {
  self ⇒
  
  def log (log: Log): IO[Unit]

  def logs (ls: Logs) = ls.toList foldMap log

  def trace (msg: ⇒ String) = log (Log trace msg)
  def debug (msg: ⇒ String) = log (Log debug msg)
  def info (msg: ⇒ String) = log (Log info msg)
  def warn (msg: ⇒ String) = log (Log warning msg)
  def error (msg: ⇒ String) = log (Log error msg)

  def filter (max: Level) =
    LoggerIO (l ⇒ (l.level >= max) ? self.log (l) | IO.ioUnit)

  def logDisRes[A](v: DisRes[A]): IO[Unit] = v fold (logNel, _ ⇒ IO.ioUnit)

  def logNel (ss: NonEmptyList[String]): IO[Unit] = ss foldMap (error(_))

  def logVal[A] (i: ValLogIO[A], default: A): IO[A] = logValV(i) | default

  def logValRes[A](v: ValRes[A]): IO[Unit] = logDisRes (v.disjunction)
    
  def logValZ[A:Default] (i: ValLogIO[A]): IO[A] = logVal (i, !!!)
    
  def logValM[A:Monoid] (i: ValLogIO[A]): IO[A] = logVal (i, ∅[A])

  def logValV[A] (i: ValLogIO[A]): EitherT[IO,NonEmptyList[String],A] = {
    def res = for {
      p ← i.run.run
      _ ← self logs p._1
      _ ← logDisRes (p._2)
    } yield p._2

    EitherT (res)
  }

}

object LoggerIO {
  def apply (f: Log ⇒ IO[Unit]): LoggerIO = new LoggerIO {
    def log (log: Log) = f (log)
  }

  private def color (c: String, msg: String) =
    c + msg + Console.RESET
  private val red = (s: String) ⇒ color (Console.RED, s)
  private val green = (s: String) ⇒ color (Console.GREEN, s)
  private val yellow = (s: String) ⇒ color (Console.YELLOW, s)
  private val blue = (s: String) ⇒ color (Console.BLUE, s)
  private val white = (s: String) ⇒  color (Console.WHITE, s)

  import Level._
  import IO.putStrLn

  lazy val consoleLogger = apply { log ⇒ log.level match {
      case Trace ⇒ putStrLn ("[" + white ("trace") +"] " + log.msg)
      case Debug ⇒ putStrLn ("[" + blue ("debug") +"] " + log.msg)
      case Info ⇒ putStrLn ("[" + green ("info") +"] " + log.msg)
      case Warning ⇒ putStrLn ("[" + yellow ("warning") +"] " + log.msg)
      case Error ⇒ putStrLn ("[" + red ("error") +"] " + log.msg)
    }
  }


  implicit val LoggerIOMonoid = new Monoid[LoggerIO]{
    val zero = LoggerIO(_ ⇒ IO.ioUnit)
    def append (a: LoggerIO, b: ⇒ LoggerIO): LoggerIO = 
      LoggerIO(l ⇒ a.log(l) >> b.log(l))
  }
}

// vim: set ts=2 sw=2 et:
