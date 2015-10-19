package efa.io

import efa.core._, Default._
import scalaz._, Scalaz._, effect._

sealed trait LoggerIO {
  self ⇒
  
  def log(log: Log): IO[Unit]
  
  def logNel(ss: NonEmptyList[String]): IO[Unit]

  def logs(ls: Logs) = ls.toList foldMap log

  def trace(msg: ⇒ String) = log(Log trace msg)

  def debug(msg: ⇒ String) = log(Log debug msg)

  def info(msg: ⇒ String) = log(Log info msg)

  def warn(msg: ⇒ String) = log(Log warning msg)

  def error(msg: ⇒ String) = log(Log error msg)

  def filter (max: Level) =
    LoggerIO(l ⇒ (l.level >= max) ? self.log (l) | IO.ioUnit, logNel)

  def logDisRes[A](v: DisRes[A]): IO[Unit] = v fold (logNel, _ ⇒ IO.ioUnit)

  def logValRes[A](v: ValRes[A]): IO[Unit] = logDisRes(v.disjunction)

  def logDis[A](i: LogDisIO[A], default: A): IO[A] = logDisV(i) | default
    
  def logDisD[A:Default](i: LogDisIO[A]): IO[A] = logDis(i, !!![A])
    
  def logDisZ[A:Monoid](i: LogDisIO[A]): IO[A] = logDis(i, ∅[A])

  def logDisV[A](i: LogDisIO[A]): DisIO[A] = {
    def res = for {
      r ← i.run(this).run
      _ ← logDisRes(r)
    } yield r

    EitherT(res)
  }
}

object LoggerIO {
  def apply (l: Log ⇒ IO[Unit], lNel: Nel[String] ⇒ IO[Unit]): LoggerIO = new LoggerIO {
    def log (log: Log) = l (log)
    def logNel (ss: Nel[String]) = lNel(ss)
  }

  def logOnly (l: Log ⇒ IO[Unit]): LoggerIO = apply(l, _ foldMap (s ⇒ l(Log error s)))

  private def color (c: String, msg: String) = c + msg + Console.RESET
  private val red = (s: String) ⇒ color (Console.RED, s)
  private val green = (s: String) ⇒ color (Console.GREEN, s)
  private val yellow = (s: String) ⇒ color (Console.YELLOW, s)
  private val blue = (s: String) ⇒ color (Console.BLUE, s)
  private val white = (s: String) ⇒  color (Console.WHITE, s)

  import Level._
  import IO.putStrLn

  lazy val consoleLogger = logOnly ( log ⇒ log.level match {
      case Trace ⇒ putStrLn ("[" + white ("trace") +"] " + log.msg)
      case Debug ⇒ putStrLn ("[" + blue ("debug") +"] " + log.msg)
      case Info ⇒ putStrLn ("[" + green ("info") +"] " + log.msg)
      case Warning ⇒ putStrLn ("[" + yellow ("warning") +"] " + log.msg)
      case Error ⇒ putStrLn ("[" + red ("error") +"] " + log.msg)
    }
  )

  implicit val LoggerIOMonoid = new Monoid[LoggerIO]{
    val zero = logOnly (_ ⇒ IO.ioUnit)
    def append (a: LoggerIO, b: ⇒ LoggerIO): LoggerIO = 
      LoggerIO(l ⇒ a.log(l) >> b.log(l), nel ⇒ a.logNel(nel) >> b.logNel(nel))
  }
}

// vim: set ts=2 sw=2 et:
