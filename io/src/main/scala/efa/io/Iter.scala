package efa.io

import efa.core.{Level, Log, Logs, DisRes}
import scalaz.{Reader ⇒ _, Writer ⇒ _, _}, Scalaz._, std.indexedSeq._
import scalaz.iteratee._, Iteratee._
import scalaz.effect._

trait IterFunctions {
  import iter.EffectStep, logDisIO._

  def logThrobber[E](
    inc: Int,
    logger: LoggerIO = LoggerIO.consoleLogger,
    lvl: Level = Level.Info,
    msg: (Int, Long) ⇒ String = (i: Int, l: Long) ⇒ loc.throbberMsg(i, l))
  : IterateeT[E,IO,Unit] = 
    throbber[E](inc, (i,l) ⇒ logger log lvl.log(msg(i,l)))

  def contK[E,A](f: Input[E] ⇒ IterIO[E,A]): EffectStep[E,A] =
    success(scont(f))

  /** This can be used to implement Enumerators for resources that
    * have first to be selected by the user.
    *
    * For a couple of use cases see the enumerators defined
    * on [[efa.io.IOChooser]].
    */
  def optionEnum[In,E](g: LogDisIO[Option[In]])
                      (f: In ⇒ EnumIO[E]): EnumIO[E] = 
    new EnumeratorT[E,LogDisIO] {
      def apply[A] = ???
    }

  def optionIter[Out,E,A](g: LogDisIO[Option[Out]])
                         (f: Out ⇒ IterIO[E,A])
                         (a: ⇒ A): IterIO[E,A] = 
    vIter(
      for {
        o ← g
        x ← o.cata[EffectStep[E,A]](
               f apply _ value,
               success(sdone(a, emptyInput))
             )
      } yield x
    )

  def optionIterM[Out,E,A](g: LogDisIO[Option[Out]])
                          (f: Out ⇒ IterIO[E,A])
                          (implicit M: Monoid[A]): IterIO[E,A] = 
    optionIter(g)(f)(M.zero)

  def resourceIter[E,R:Resource]
    (create: LogDisIO[R], name: String)
    (out: (E,R) ⇒ LogDisIO[Unit]): IterIO[E,Unit] = {
      def go(r: R): EffectStep[E,Unit] = contK { i ⇒ 
        vIter(
          i.fold(
            empty = go(r),
            el    = e ⇒ out(e, r) >> go(r),
            eof   = close(r, name) >>
                    success[StepIO[E,Unit]](sdone((), eofInput))
          )
        )
      }
      
      vIter(create >>= go)
    }

  def resourceEnum[E,R:Resource]
    (r: LogDisIO[R], name: String)
    (enum: R ⇒ EnumIO[E]): EnumIO[E] = new EnumeratorT[E,LogDisIO] {
      def apply[A] = (s: StepIO[E,A]) ⇒ {
        def valStep: EffectStep[E,A] = for {
          x ← r
          s ← ensure(enum(x) apply s value, close(x, name))
        } yield s

        vIter(valStep)
      }
    }

  def throbber[E](inc: Int, out: (Int, Long) ⇒ IO[Unit])
    : IterateeT[E,IO,Unit] = {
    type ToIter = Input[E] ⇒ IterateeT[E,IO,Unit]

    def now = System.currentTimeMillis
    def cont(t: ToIter) = IO(scont(t))
    def icont(t: ToIter) = iterateeT(cont(t))
    def report(acc: Int, millis: Long) =
      IO putStrLn s"Accumulated $acc items in $millis ms"

    def step(acc: Int, cnt: Int, start: Long): ToIter = i ⇒ 
      (acc, cnt, i) match {
        case (a, 0, Input.Element(_)) ⇒ 
          iterateeT(report(a + inc, now - start) >>
          cont(step(a + inc, inc - 1, start)))
        case (a, x, Input.Element(_)) ⇒ icont(step(a, x - 1, start))
        case (a, x, Input.Empty())    ⇒ icont(step(a, x, start))
        case (a, x, Input.Eof())      ⇒ iterateeT(IO(sdone((), eofInput)))
      }

    icont(step(0, inc - 1, now))
  }

  def vIter[E,A](s: EffectStep[E,A]): IterIO[E,A] = 
    iterateeT[E,LogDisIO,A](s)
}

trait IterInstances {
  /** Monoid implementation for Iteratees
    *
    * This implementation will pass input to both appended Iteratees
    * until both are in Done state at which point the appended results
    * of both Iteratees will be returned. This is useful for concatenating
    * several data sinks. One could for instance ouput stuff to the console
    * as well as a file for instance.
    *
    * One implementation detail: This function could have been implemented
    * via function `zip` defined on `IterateeT` and then using map and
    * Monoid `append` to concatenate the zipped results. However, this
    * implementation is more memory efficient.
    */
  implicit def IterMonoid[E,F[_]:Monad,A:Monoid]: Monoid[IterateeT[E,F,A]] =
    new Monoid[IterateeT[E,F,A]] {
      type Iter[X] = IterateeT[E,F,X]
      type Stp[X] = StepT[E,F,X]

      val zero = ∅[A].η[Iter]

      def append(a: Iter[A], b: ⇒ Iter[A]): Iter[A] = iterateeT[E,F,A](
        for {
          sta ← a.value
          stb ← b.value
        } yield stepConcat(sta, stb)
      )

      import StepT.{Cont, Done}
      def stepConcat(a: Stp[A], b: Stp[A]): Stp[A] = (a, b) match {
        case (Cont(fa), Cont(fb))     ⇒ scont(i ⇒ append(fa(i), fb(i)))
        case (Cont(fa), b@Done(_, _)) ⇒ scont(i ⇒ append(fa(i), b.pointI))
        case (a@Done(_, _), Cont(fb)) ⇒ scont(i ⇒ append(a.pointI, fb(i)))
        case (Done(a, i), Done(b, j)) ⇒ sdone(a ⊹ b, i)
      }
    }
}

object iter extends IterFunctions with IterInstances {
  type EffectStep[E,A] = LogDisIO[StepIO[E,A]]
}

// vim: set ts=2 sw=2 et:
