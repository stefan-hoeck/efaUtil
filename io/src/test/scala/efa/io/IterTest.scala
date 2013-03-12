package efa.io

import efa.core.DisRes
import org.scalacheck._, Prop._
import scalaz._, Scalaz._, effect._, iteratee._, Iteratee._

object IterTest extends Properties("IterFunctions") with IterFunctions {
  import EfaIO._

  final val Boo = "BOO!"
  //consumes all input and accumulates in a list
  val all: IterIO[String,List[String]] = consume

  //consumes nothing and returns empty list
  val none: IterIO[String,List[String]] = done(Nil, emptyInput)

  //fails upon first call
  val error: IterIO[String,List[String]] = vIter(fail(Boo))

  //consumes one element and fails
  val errorInBetween: IterIO[String,List[String]] = 
    vIter(contI[String,List[String]](_ ⇒ fail(Boo), fail(Boo)))

  //generates no input
  val noLines: EnumIO[String] = empty[String,LogDisIO]

  //generates lines in ls
  def lines(ls: List[String]): EnumIO[String] = enumList(ls)

  //fails upon generating first line
  val errorEnum: EnumIO[String] = new EnumeratorT[String,LogDisIO] {
    def apply[A] = (s: StepIO[String,A]) ⇒ vIter(fail(Boo))
  }

  //fails after generating ten lines
  def errorInBetweenEnum: EnumIO[String] =
    new RecursiveEnumIO[String](_ ⇒ Boo) {
      private[this] var count = 0
      protected def next(): Option[String] = {
        if (count < 10) { count += 1; Boo.some }
        else throw new IllegalArgumentException(Boo)
      }
    }


  val linesG = Gen listOf Gen.identifier

  lazy val logger = ∅[LoggerIO]
  //lazy val logger = LoggerIO.consoleLogger

  def testIO[A](i: LogDisIO[A]): DisRes[A] =
    logger.logDisV(i).run.unsafePerformIO

  //make sure that resources are closed and
  //input is accumulated properly
  property("resourceEnum_consumed") = forAll(linesG) { ls ⇒ 
    val bs = Bytes(ls)

    //consumes all lines in bs
    val res = testIO(all &= bs.lines run)

    (res ≟ ls.right) :| s"lines read correctly" &&
    (bs.wasOpened) :| "resource was opened" &&
    (bs.isClosed) :| "resource was closed"
  }

  //make sure that resources are neither opened nor closed
  //if interatee is already in 'done' state
  property("resourceEnum_done") = forAll(linesG) { ls ⇒ 
    val bs = Bytes(ls)

    //consumes nothing since already done
    val res = testIO(none &= bs.lines run)

    (res ≟ Nil.right) :| "lines read correctly" &&
    (!bs.wasOpened) :| "resource was never opened" &&
    (!bs.isClosed) :| "resource was never closed"
  }

  //make sure that resources are neither opened nor closed
  //if iteratee fails upon initialization
  property("resourceEnum_error") = forAll(linesG) { ls ⇒ 
    val bs = Bytes(ls)

    //failure before we even start
    val res = testIO(error &= bs.lines run)

    (res ≟ Boo.wrapNel.left) :| "error while reading" &&
    (!bs.wasOpened) :| "resource was never opened" &&
    (!bs.isClosed) :| "resource was never closed"
  }

  //make sure that resources closed even after a failure
  //in the iteratee
  property("resourceEnum_errorInBetween") = forAll(linesG) { ls ⇒ 
    val bs = Bytes(ls)

    //failure after first element
    val res = testIO(errorInBetween &= bs.lines run)

    (res ≟ Boo.wrapNel.left) :| "error while reading" &&
    (bs.wasOpened) :| "resource was opened" &&
    (bs.isClosed) :| "resource was closed"
  }

  property("optionEnum_consumed") = forAll(linesG) { ls ⇒ 
    val bs = Bytes(ls)

    //consumes all lines in bs
    val res = testIO(all &= optionEnum(success(bs.some))(_.lines) run)

    (res ≟ ls.right) :| "lines read correctly" &&
    (bs.wasOpened) :| "resource was opened" &&
    (bs.isClosed) :| "resource was closed"
  }

  property("optionEnum_done") = forAll(linesG) { ls ⇒ 
    var initialized = false
    val bs = Bytes(ls)

    def get: LogDisIO[Option[Bytes]] = success{initialized = true; bs.some}

    //consumes nothing since already done
    val res = testIO(none &= optionEnum(get)(_.lines) run)

    (res ≟ Nil.right) :| "lines read correctly" &&
    (!bs.wasOpened) :| "resource was opened" &&
    (!bs.isClosed) :| "resource was closed" &&
    (!initialized) :| "resource wasn't initialized"
  }

  property("optionEnum_error") = forAll(linesG) { ls ⇒ 
    var initialized = false
    val bs = Bytes(ls)

    def get: LogDisIO[Option[Bytes]] = success{initialized = true; bs.some}

    //failure before we even start
    val res = testIO(error &= optionEnum(get)(_.lines) run)

    (res ≟ Boo.wrapNel.left) :| "error during reading" &&
    (!bs.wasOpened) :| "resource was never opened" &&
    (!bs.isClosed) :| "resource was never closed" &&
    (!initialized) :| "resource wasn't initialized"
  }

  property("optionEnum_errorInBetween") = forAll(linesG) { ls ⇒ 
    var initialized = false
    val bs = Bytes(ls)

    def get: LogDisIO[Option[Bytes]] = success{initialized = true; bs.some}

    //failure after first element
    val res = testIO(errorInBetween &= optionEnum(get)(_.lines) run)

    (res ≟ Boo.wrapNel.left) :| "error during reading" &&
    (bs.wasOpened) :| "resource was opened" &&
    (bs.isClosed) :| "resource was closed" &&
    (initialized) :| "resource was initialized"
  }

  property("optionEnum_none_consumed") = {
    var initialized = false

    def get: LogDisIO[Option[Bytes]] = success{initialized = true; None}

    //consumes everything which is nothing at all
    val res = testIO(all &= optionEnum(get)(_.lines) run)

    (res ≟ Nil.right) :| "lines read correctly" &&
    (initialized) :| "resource was initialized"
  }

  //checks that resource is closed after processing input
  property("resourceIter_all") = forAll(linesG) { ls ⇒ 
    val bs = new BytesOut

    val res = testIO(bs.linesI() &= lines(ls) run)

    //println(s"Lines: $ls; result: $res; read: ${bs.getLines}")

    (res.isRight) :| "no error occured" &&
    (bs.getLines ≟ ls) :| "lines processed correctly" &&
    (bs.wasOpened ≟ ls.nonEmpty) :| "resource was opened" &&
    (bs.isClosed ≟ ls.nonEmpty) :| "resource was closed"
  }

  //checks that resource is not opened nor closed if no input is available
  property("resourceIter_empty") = {
    val bs = new BytesOut

    val res = testIO(bs.linesI() &= noLines run)

    (res.isRight) :| "no error occured" &&
    (bs.getLines ≟ Nil) :| "lines processed correctly" &&
    (!bs.wasOpened) :| "resource was never opened" &&
    (!bs.isClosed) :| "resource was never closed"
  }

  //checks that resource is not opened nor closed if error happens
  //at first input
  property("resourceIter_error") = {
    val bs = new BytesOut

    val res = testIO(bs.linesI() &= errorEnum run)

    (res.isLeft) :| "error occured" &&
    (!bs.wasOpened) :| "resource was never opened" &&
    (!bs.isClosed) :| "resource was never closed"
  }

  //checks that resource is closed if an error occurs in the enumerator
  property("resourceIter_errorInBetween") = {
    val bs = new BytesOut

    val res = testIO(bs.linesI() &= errorInBetweenEnum run)

    (res.isLeft) :| "error occured" &&
    (bs.wasOpened) :| "resource was opened" &&
    (bs.isClosed) :| "resource was closed"
  }

  //checks that resource is closed if error happens during write
  property("resourceIter_errorOnWrite") = {
    val bs = new BytesOut
    var count = 0

    //error occurs after ten successful writes
    val it = iter.resourceIter[String,BytesOut](
      success{ bs.open(); bs }, bs.toString
    )((s,b) ⇒ 
      if (count < 10) success{ count += 1 } else fail(Boo)
    )

    val res = testIO(it &= lines(List.fill(100)("")) run)

    (res.isLeft) :| "error occured" &&
    (bs.wasOpened) :| "resource was opened" &&
    (bs.isClosed) :| "resource was closed"
  }

  //checks that resource is closed after processing input
  property("optionIter_all") = forAll(linesG) { ls ⇒ 
    val bs = new BytesOut

    val get = success{ bs.some }
    val it = optionIterM(get)(_.linesI()) 
    val res = testIO(it &= lines(ls) run)

    (res.isRight) :| "no error occured" &&
    (bs.getLines ≟ ls) :| "lines processed correctly" &&
    (bs.wasOpened ≟ ls.nonEmpty) :| "resource was opened" &&
    (bs.isClosed ≟ ls.nonEmpty) :| "resource was closed"
  }

  //checks that resource is not opened nor closed if no input is available
  property("optionIter_empty") = {
    val bs = new BytesOut
    var init = false

    val get = success{ init = true; bs.some }
    val it = optionIterM(get)(_.linesI()) 
    val res = testIO(it &= noLines run)

    (res.isRight) :| "no error occured" &&
    (bs.getLines ≟ Nil) :| "lines processed correctly" &&
    (!bs.wasOpened) :| "resource was never opened" &&
    (!bs.isClosed) :| "resource was never closed" &&
    (!init) :| "resource was never initialized"
  }

  //checks that resource is not opened nor closed if error happens
  //at first input
  property("resourceIter_error") = {
    val bs = new BytesOut
    var init = false

    val get = success{ init = true; bs.some }
    val it = optionIterM(get)(_.linesI()) 
    val res = testIO(it &= errorEnum run)

    (res.isLeft) :| "error occured" &&
    (!bs.wasOpened) :| "resource was never opened" &&
    (!bs.isClosed) :| "resource was never closed" &&
    (!init) :| "resource was never initialized"
  }

  //checks that resource is closed if an error occurs in the enumerator
  property("resourceIter_errorInBetween") = {
    val bs = new BytesOut
    var init = false

    val get = success{ init = true; bs.some }
    val it = optionIterM(get)(_.linesI()) 
    val res = testIO(it &= errorInBetweenEnum run)

    (res.isLeft) :| "error occured" &&
    (bs.wasOpened) :| "resource was opened" &&
    (bs.isClosed) :| "resource was closed" &&
    (init) :| "resource was initialized"
  }
}

// vim: set ts=2 sw=2 et:
