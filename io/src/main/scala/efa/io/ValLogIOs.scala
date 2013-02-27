package efa.io

import efa.core._
import scalaz._, Scalaz._, effect._, iteratee.IterateeT

trait ValLogIOFunctions {
  val logM = Monad[LogIO]
  val valM = Monad[ValLogIO]

  val noLogs: Logs = DList()

  val nullValLogIO: ValLogIO[Unit] = point(())

  def point[A] (a: ⇒ A): ValLogIO[A] = valM point a

  def lift[A] (a: IO[(Logs,DisRes[A])]): ValLogIO[A] =
    EitherT[LogIO,NonEmptyList[String],A](WriterT(a))

  def liftIO[A] (i: IO[A]): ValLogIO[A] =
    liftLogIO (WriterT(i map ((noLogs, _))))

  def liftDisIO[A] (i: IO[DisRes[A]]): ValLogIO[A] =
    lift(i map (dr ⇒ (noLogs, dr)))

  def liftLogIO[A] (i: LogIO[A]): ValLogIO[A] = EitherT right i

  def liftDis[A] (i: DisRes[A]): ValLogIO[A] = liftDisIO(IO(i))

  def mapW[A](a: ValLogIO[A])(f: LogIO[DisRes[A]] ⇒ LogIO[DisRes[A]])
    : ValLogIO[A] = EitherT(f(a.run))

  def mapIO[A](a: ValLogIO[A])
    (f: IO[(Logs,DisRes[A])] ⇒ IO[(Logs,DisRes[A])])
    : ValLogIO[A] = mapW(a)(w ⇒ WriterT(f(w.run)))

  def log (msg: ⇒ String, level: Level): ValLogIO[Unit] =
    mapW(nullValLogIO)(_ :++> DList(Log log (msg, level)))

  def trace (msg: ⇒ String) = log (msg, Level.Trace)

  def debug (msg: ⇒ String) = log (msg, Level.Debug)

  def info (msg: ⇒ String) = log (msg, Level.Info)

  def warning (msg: ⇒ String) = log (msg, Level.Warning)

  def error (msg: ⇒ String) = log (msg, Level.Error)

  def except[A] (e: ValLogIO[A], msg: Throwable ⇒ String): ValLogIO[A] =
    mapIO(e)(_ except (t ⇒ IO((noLogs , msg(t).wrapNel.left[A]))))

  def ensure[A] (e: ValLogIO[A], f: ValLogIO[Unit]): ValLogIO[A] = {
    def ens (io: IO[(Logs,DisRes[A])]): IO[(Logs,DisRes[A])] = for {
      v ← io onException f.run.run
      p ← f.run.run
    } yield (v._1 ++ p._1, v._2)

    mapIO(e)(ens)
  }

  def fail[A](s: ⇒ String): ValLogIO[A] = failNel(s.wrapNel)

  def failK[A](s: ⇒ String): LogToDisIO[A] = failNelK(s.wrapNel)

  def failNel[A](s: ⇒ NonEmptyList[String]): ValLogIO[A] =
    EitherT left logM.point(s)

  def failNelK[A](s: ⇒ NonEmptyList[String]): LogToDisIO[A] =
    toLogKleisli(failNel(s))

  def failIter[E,A](s: ⇒ String): IterIO[E,A] = failNelIter(s.wrapNel)

  def failNelIter[E,A](s: ⇒ NonEmptyList[String]): IterIO[E,A] =
    IterateeT.iterateeT[E,LogToDisIO,A](failNelK(s))
      

  def success[A] (a: ⇒ A): ValLogIO[A] = EitherT right logM.point(a)

  def successK[A] (a: ⇒ A): LogToDisIO[A] = toLogKleisli(success(a))

  def validate[A,B](vli: ValLogIO[A])(v: Validator[A,B]): ValLogIO[B] =
    lift (vli.run.run map {case(a,b) ⇒ (a, b flatMap v.run)})

  /**
    * Closes a resource
    */
   def close[A:Resource](c: A): ValLogIO[Unit] = {
     def cl: IO[Unit] = Resource[A] close c except (_ ⇒ IO.ioUnit)

     liftIO(cl) >> debug(loc closed c.toString)
   }

  /**
    * Performs some action with a given Resource. Exceptions are caught and
    * wrapped as messages in a DisRes. The given Close is closed in
    * the end, no matter whether an exception was raised or not.
    */
  def withClose[C:Resource,A]
    (c: ⇒ C, msg: ⇒ String)
    (f: C ⇒ ValLogIO[A]): ValLogIO[A] =
    ensure(except(f(c), t ⇒ s"${msg}: ${t.toString}"), close(c))

  def toLogKleisli[A](v: ValLogIO[A]): LogToDisIO[A] =
    Kleisli[DisIO,LoggerIO,A](_ logValV v)

  def fromLogKleisli[A](v: LogToDisIO[A], l: LoggerIO): ValLogIO[A] =
    liftDisIO(v run l run)
}

trait ValLogIOInstances {
  implicit def ValLogIOMonoid[A:Monoid] = Monoid.liftMonoid[ValLogIO,A]
}

object valLogIO extends ValLogIOFunctions with ValLogIOInstances

// vim: set ts=2 sw=2 et:
