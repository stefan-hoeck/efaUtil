package efa.io

import efa.core.{DisRes, Level, Validator}
import scalaz._, Scalaz._, effect._, scalaz.iteratee.IterateeT

trait LogDisIOFunctions {
  val logM = Monad[LogDisIO]

  def point[A](a: ⇒ A): LogDisIO[A] = logM point a

  def lift[A](a: LoggerIO ⇒ IO[DisRes[A]]): LogDisIO[A] =
    Kleisli[DisIO,LoggerIO,A](l ⇒ EitherT(a(l)))

  def liftIO[A](i: IO[A]): LogDisIO[A] = liftDisIO(i map { _.right })

  def liftLogIO[A](i: LoggerIO ⇒ IO[A]): LogDisIO[A] = 
    lift(i andThen { _ map { _.right } })

  def liftDisIO[A](i: IO[DisRes[A]]): LogDisIO[A] = lift(_ ⇒ i)

  def liftDis[A](i: DisRes[A]): LogDisIO[A] = liftDisIO(IO(i))

  def mapIO[A](a: LogDisIO[A])(f: IO[DisRes[A]] ⇒ IO[DisRes[A]])
    : LogDisIO[A] = lift(l ⇒ f(a run l run))

  def except[A](e: LogDisIO[A], msg: Throwable ⇒ String): LogDisIO[A] =
    mapIO(e)(_ except (t ⇒ IO(msg(t).wrapNel.left[A])))

  def ensure[A](e: LogDisIO[A], f: LogDisIO[Unit]): LogDisIO[A] = {
    def ens(l: LoggerIO): IO[DisRes[A]] = for {
      v ← e.run(l).run onException f.run(l).run
      _ ← f.run(l)run
    } yield v

    lift(ens)
  }

  def log(msg: ⇒ String, level: Level): LogDisIO[Unit] =
    lift { _ log level.log(msg) map (_.right) }

  def trace(msg: ⇒ String) = log(msg, Level.Trace)

  def debug(msg: ⇒ String) = log(msg, Level.Debug)

  def info(msg: ⇒ String) = log(msg, Level.Info)

  def warning(msg: ⇒ String) = log(msg, Level.Warning)

  def error(msg: ⇒ String) = log(msg, Level.Error)

  def fail[A](s: ⇒ String): LogDisIO[A] = failNel(s.wrapNel)

  def failNel[A](s: ⇒ NonEmptyList[String]): LogDisIO[A] =
    liftDisIO(IO(s.left))

  def failIter[E,A](s: ⇒ String): IterIO[E,A] = failNelIter(s.wrapNel)

  def failNelIter[E,A](s: ⇒ NonEmptyList[String]): IterIO[E,A] =
    IterateeT.iterateeT[E,LogDisIO,A](failNel(s))
      
  def success[A](a: ⇒ A): LogDisIO[A] = point(a)

  /**
    * Closes a resource
    */
  def close[A:Resource](c: A, name: String): LogDisIO[Unit] = {
    def cl: IO[Unit] = Resource[A] close c except (_ ⇒ IO.ioUnit)

    liftIO(cl) >> debug(loc closed name)
  }

  def validate[A,B](vli: LogDisIO[A])(v: Validator[A,B]): LogDisIO[B] =
    lift(l ⇒ vli.run(l).run map { _ flatMap v.run })
}

trait LogDisIOInstances {
  implicit def LogDisIOMonoid[A:Monoid] = Monoid.liftMonoid[LogDisIO,A]
}

object logDisIO extends LogDisIOFunctions with LogDisIOInstances

// vim: set ts=2 sw=2 et:
