package efa.core.std

import scala.language.experimental.macros
import scalaz.{Equal,Monoid,Semigroup, Cord, Order,
               Show, Ordering, Apply, Applicative, Lens}
import scalaz.syntax.applicative._
import shapeless._
import shapeless.ops.function._
import shapeless.syntax.std.function._
import org.scalacheck.{Gen, Arbitrary, Shrink}

/** The functionality below was copy-pasted from
  * shapeless contrib and should become obsolete
  * once shapeless-contrib goes to 2.11 / 7.1.0
  */
trait ShapelessFunctions {
}

trait ShapelessInstances {
  implicit class ApplicativeOps[G[_]](instance: Applicative[G]) {
    def liftA[F, R, I <: HList, GI <: HList, OF](f: F)(
      implicit hlister: FnToProduct.Aux[F, I => R],
               lifter: LifterAux[G, I, R, GI],
               unhlister: FnFromProduct.Aux[GI => G[R], OF]
    ): OF =
      lifter(instance.pure(f.toProduct))(instance).fromProduct
  }

  // TODO this is terrible
  private lazy val _emptyCoproduct: Gen[Nothing] = Gen.fail

  implicit def ArbitraryI: TypeClass[Arbitrary] = new TypeClass[Arbitrary] {

    def emptyProduct = Arbitrary(Gen.const(HNil))

    def product[H, T <: HList](h: Arbitrary[H], t: Arbitrary[T]) =
      Arbitrary(Gen.sized { size =>
        if (size == 0)
          Gen.fail
        else {
          val resizedH = Gen.resize(size.abs/2, h.arbitrary)
          val resizedT = Gen.resize(size.abs - size.abs/2, t.arbitrary)
          for { h <- resizedH; t <- resizedT }
            yield h :: t
        }})

    def coproduct[L, R <: Coproduct](l: => Arbitrary[L], r: => Arbitrary[R]) = {
      val rGen = r.arbitrary
      val gens: List[Gen[L :+: R]] =
        l.arbitrary.map(Inl(_): L :+: R) ::
        (if (rGen == _emptyCoproduct) Nil else List(rGen.map(Inr(_): L :+: R)))
      Arbitrary(Gen.oneOf(gens).flatMap(identity))
    }

    def emptyCoproduct =
      Arbitrary(_emptyCoproduct)

    def project[A, B](b: => Arbitrary[B], ab: A => B, ba: B => A) =
      Arbitrary(b.arbitrary.map(ba))

  }

  implicit def ShrinkI: TypeClass[Shrink] = new TypeClass[Shrink] {

    def emptyProduct = Shrink(_ => Stream.empty)

    def product[F, T <: HList](f: Shrink[F], t: Shrink[T]) = Shrink { case a :: b ⇒
      f.shrink(a).map( _ :: b) append
      t.shrink(b).map(a :: _)
    }

    def project[A, B](b: => Shrink[B], ab: A => B, ba: B => A) = Shrink { a =>
      b.shrink(ab(a)).map(ba)
    }

    def coproduct[L, R <: Coproduct](sl: => Shrink[L], sr: => Shrink[R]) = Shrink { lr =>
      lr match {
        case Inl(l) ⇒ sl.shrink(l).map(Inl.apply)
        case Inr(r) ⇒ sr.shrink(r).map(Inr.apply)
      }
    }

    def emptyCoproduct: Shrink[CNil] = Shrink(_ => Stream.empty)

  }

  def deriveArbitrary[T](implicit ev: TypeClass[Arbitrary]): Arbitrary[T] =
    macro GenericMacros.deriveInstance[Arbitrary, T]


  def deriveShrink[T](implicit ev: TypeClass[Shrink]): Shrink[T] =
    macro GenericMacros.deriveInstance[Shrink, T]

  // Instances

  implicit def SemigroupI: ProductTypeClass[Semigroup] = new ProductTypeClass[Semigroup] with Empty {
    def product[F, T <: HList](f: Semigroup[F], t: Semigroup[T]) =
      new ProductSemigroup[F, T] { def F = f; def T = t }
    def project[A, B](b: => Semigroup[B], ab: A => B, ba: B => A) =
      new IsomorphicSemigroup[A, B] { def B = b; def to = ab; def from = ba }
  }

  implicit def MonoidI: ProductTypeClass[Monoid] = new ProductTypeClass[Monoid] with Empty {
    def product[F, T <: HList](f: Monoid[F], t: Monoid[T]) =
      new ProductMonoid[F, T] { def F = f; def T = t }
    def project[A, B](b: => Monoid[B], ab: A => B, ba: B => A) =
      new IsomorphicMonoid[A, B] { def B = b; def to = ab; def from = ba }
  }

  implicit def EqualI: TypeClass[Equal] = new TypeClass[Equal] with Empty {
    def product[F, T <: HList](f: Equal[F], t: Equal[T]) =
      new ProductEqual[F, T] { def F = f; def T = t }
    def coproduct[L, R <: Coproduct](l: => Equal[L], r: => Equal[R]) =
      new SumEqual[L, R] { def L = l; def R = r }
    def project[A, B](b: => Equal[B], ab: A => B, ba: B => A) =
      new IsomorphicEqual[A, B] { def B = b; def to = ab; def from = ba }
  }

  implicit def ShowI: TypeClass[Show] = new TypeClass[Show] with Empty {
    def product[F, T <: HList](f: Show[F], t: Show[T]) =
      new ProductShow[F, T] { def F = f; def T = t }
    def coproduct[L, R <: Coproduct](l: => Show[L], r: => Show[R]) =
      new SumShow[L, R] { def L = l; def R = r }
    def project[A, B](b: => Show[B], ab: A => B, ba: B => A) =
      new IsomorphicShow[A, B] { def B = b; def to = ab; def from = ba }
  }

  implicit def OrderI: TypeClass[Order] = new TypeClass[Order] with Empty {
    def product[F, T <: HList](f: Order[F], t: Order[T]) =
      new ProductOrder[F, T] { def F = f; def T = t }
    def coproduct[L, R <: Coproduct](l: => Order[L], r: => Order[R]) =
      new SumOrder[L, R] { def L = l; def R = r }
    def project[A, B](b: => Order[B], ab: A => B, ba: B => A) =
      new IsomorphicOrder[A, B] { def B = b; def to = ab; def from = ba }
  }


  // Boilerplate

  def deriveSemigroup[T](implicit ev: ProductTypeClass[Semigroup]): Semigroup[T] =
    macro GenericMacros.deriveProductInstance[Semigroup, T]

  def deriveMonoid[T](implicit ev: ProductTypeClass[Monoid]): Monoid[T] =
    macro GenericMacros.deriveProductInstance[Monoid, T]

  def deriveEqual[T](implicit ev: TypeClass[Equal]): Equal[T] =
    macro GenericMacros.deriveInstance[Equal, T]

  def deriveOrder[T](implicit ev: TypeClass[Order]): Order[T] =
    macro GenericMacros.deriveInstance[Order, T]

  def deriveShow[T](implicit ev: TypeClass[Show]): Show[T] =
    macro GenericMacros.deriveInstance[Show, T]

  implicit def shapelessLensOps[A, B](l: shapeless.Lens[A, B]) = new LensOps[A, B] {
    override val asShapeless = l
  }
}

trait Product[+C[_], F, T <: HList] {
  def F: C[F]
  def T: C[T]

  type λ = F :: T
}

trait Sum[+C[_], L, R <: Coproduct] {
  def L: C[L]
  def R: C[R]

  type λ = L :+: R
}

trait Isomorphic[+C[_], A, B] {
  def B: C[B]
  def to: A => B
  def from: B => A
}

private trait Empty {

  def emptyProduct = new Monoid[HNil] with Order[HNil] with Show[HNil] {
    def zero = HNil
    def append(f1: HNil, f2: => HNil) = HNil
    override def equal(a1: HNil, a2: HNil) = true
    def order(x: HNil, y: HNil) = Monoid[Ordering].zero
    override def shows(f: HNil) = "HNil"
  }

  def emptyCoproduct = new Monoid[CNil] with Order[CNil] with Show[CNil] {
    def zero = ???
    def append(f1: CNil, f2: => CNil) = f1
    def order(x: CNil, y: CNil) = Monoid[Ordering].zero
  }

}

// Products

private trait ProductSemigroup[F, T <: HList]
  extends Semigroup[F :: T]
  with Product[Semigroup, F, T] {

  def append(f1: λ, f2: => λ) =
    F.append(f1.head, f2.head) :: T.append(f1.tail, f2.tail)

}

private trait ProductMonoid[F, T <: HList]
  extends ProductSemigroup[F, T]
  with Monoid[F :: T]
  with Product[Monoid, F, T] {

  def zero = F.zero :: T.zero

}

private trait ProductEqual[F, T <: HList]
  extends Equal[F :: T]
  with Product[Equal, F, T] {

  def equal(a1: λ, a2: λ) =
    F.equal(a1.head, a2.head) && T.equal(a1.tail, a2.tail)

}

private trait ProductOrder[F, T <: HList]
  extends ProductEqual[F, T]
  with Order[F :: T]
  with Product[Order, F, T] {

  override def equal(a1: λ, a2: λ) =
    super[ProductEqual].equal(a1, a2)

  def order(x: λ, y: λ) =
    Semigroup[Ordering].append(F.order(x.head, y.head), T.order(x.tail, y.tail))

}

private trait ProductShow[F, T <: HList]
  extends Show[F :: T]
  with Product[Show, F, T] {

  override def shows(f: λ) =
    F.shows(f.head) ++ " :: " ++ T.shows(f.tail)

  override def show(f: λ) =
    F.show(f.head) ++ Cord(" :: ") ++ T.show(f.tail)

}

// Coproducts

private trait SumEqual[L, R <: Coproduct]
  extends Equal[L :+: R]
  with Sum[Equal, L, R] {

  def equal(a1: λ, a2: λ) = (a1, a2) match {
    case (Inl(l1), Inl(l2)) => L.equal(l1, l2)
    case (Inr(r1), Inr(r2)) => R.equal(r1, r2)
    case _ => false
  }

}

private trait SumOrder[L, R <: Coproduct]
  extends SumEqual[L, R]
  with Order[L :+: R]
  with Sum[Order, L, R] {

  override def equal(a1: λ, a2: λ) =
    super[SumEqual].equal(a1, a2)

  def order(x: λ, y: λ) = (x, y) match {
    case (Inl(a), Inl(b)) => L.order(a, b)
    case (Inl(_), Inr(_)) => Ordering.LT
    case (Inr(_), Inl(_)) => Ordering.GT
    case (Inr(a), Inr(b)) => R.order(a, b)
  }

}

private trait SumShow[L, R <: Coproduct]
  extends Show[L :+: R]
  with Sum[Show, L, R] {

  override def shows(f: λ) = f match {
    case Inl(l) => s"Inl(${L.shows(l)})"
    case Inr(r) => s"Inr(${R.shows(r)})"
  }

  override def show(f: λ) = f match {
    case Inl(l) => Cord("Inl(") ++ L.show(l) ++ Cord(")")
    case Inr(r) => Cord("Inr(") ++ R.show(r) ++ Cord(")")
  }

}

// Isos

private trait IsomorphicSemigroup[A, B]
  extends Semigroup[A]
  with Isomorphic[Semigroup, A, B] {

  def append(f1: A, f2: => A) =
    from(B.append(to(f1), to(f2)))

}

private trait IsomorphicMonoid[A, B]
  extends IsomorphicSemigroup[A, B]
  with Monoid[A]
  with Isomorphic[Monoid, A, B] {

  def zero = from(B.zero)

}

private trait IsomorphicEqual[A, B]
  extends Equal[A]
  with Isomorphic[Equal, A, B] {

  override def equal(a1: A, a2: A) =
    B.equal(to(a1), to(a2))

}

private trait IsomorphicOrder[A, B]
  extends IsomorphicEqual[A, B]
  with Order[A]
  with Isomorphic[Order, A, B] {

  override def equal(a1: A, a2: A) =
    super[IsomorphicEqual].equal(a1, a2)

  def order(x: A, y: A) =
    B.order(to(x), to(y))

}

private trait IsomorphicShow[A, B]
  extends Show[A]
  with Isomorphic[Show, A, B] {

  override def shows(f: A) =
    B.shows(to(f))

  override def show(f: A) =
    B.show(to(f))

}

trait LifterAux[G[_], I <: HList, R, GI <: HList] {
  def apply(gf: G[I => R])(implicit G: Apply[G]): GI => G[R]
}

object LifterAux {

  implicit def liftZero[G[_], R]: LifterAux[G, HNil, R, HNil] = new LifterAux[G, HNil, R, HNil] {
    def apply(gf: G[HNil => R])(implicit G: Apply[G]) = _ =>
      gf map { _(HNil) }
  }

  implicit def liftCons[G[_], H, T <: HList, R, GI <: HList](implicit tail: LifterAux[G, T, R, GI]): LifterAux[G, H :: T, R, G[H] :: GI] = new LifterAux[G, H :: T, R, G[H] :: GI] {
    def apply(gf: G[(H :: T) => R])(implicit G: Apply[G]) = {
      case gh :: gi =>
        tail(G.apply2(gh, gf) { (h, f) => t => f(h :: t) })(G)(gi)
    }
  }

}

trait LensOps[A, B] {

  def asZ: scalaz.Lens[A, B] =
    scalaz.LensFamily.lensg(asShapeless.set, asShapeless.get)

  def asShapeless: shapeless.Lens[A, B] =
    new shapeless.Lens[A, B] {
      def get(a: A): B = asZ.get(a)
      def set(a: A)(b: B): A = asZ.set(a, b)
    }

}

// vim: set ts=2 sw=2 et:
