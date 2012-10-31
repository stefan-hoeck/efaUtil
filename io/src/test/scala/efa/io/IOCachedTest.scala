package efa.io

import scalaz._, Scalaz._, effect._
import org.scalacheck._, Prop._

object IOCachedTest extends Properties ("IOCached") {

  val iGen = Gen choose (1, 100)
  property ("generator") = Prop.forAll (iGen) { i ⇒  
    val expectedValues = false :: List.fill (i - 1)(true)
    val res = for {
      gen ← Generator.create
      bools ← List.fill(i)(gen.generate).sequence
    } yield (bools ≟ expectedValues)

    res.unsafePerformIO
  }

  property ("immutable") = Prop.forAll (iGen) { i ⇒ 
    val firstExpected = List.fill (i)(false)
    val secondExpected = List.fill (i)(true)
    val res = for {
      gen ← Generator.create
      immFirst = IOCached apply gen.generate
      immSecond = IOCached apply gen.generate
      first ← List.fill (i)(immFirst.get).sequence
      second ← List.fill (i)(immSecond.get).sequence
    } yield (first ≟ firstExpected) && (second ≟ secondExpected)

    res.unsafePerformIO
  }
}

private[io] class Generator (generated: IORef[Boolean]) {
  def generate: IO[Boolean] = for {
    g ← generated.read
    _ ← g ? IO.ioUnit | generated.write(true)
  } yield g
}

object Generator {
  def apply (m: IORef[Boolean]): IO[Generator] = IO (new Generator (m))

  val create: IO[Generator] = IO.newIORef (false) >>= apply 
}

// vim: set ts=2 sw=2 et:
