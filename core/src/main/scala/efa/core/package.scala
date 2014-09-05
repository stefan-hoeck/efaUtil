package efa

import scalaz._
import efa.core.spi.UtilLoc

package object core {

  lazy val loc = Service.unique[UtilLoc]

  type Nel[A] = NonEmptyList[A]

  type Logs = DList[Log]

  type DisRes[A] = Nel[String] \/ A

  type ValRes[A] = Validation[Nel[String],A]

  type Validator[R,A] = Kleisli[DisRes,R,A]

  type ValSt[A] = ValRes[State[A,Unit]]
  
  type EndoVal[A] = Validator[A,A]

  //Aliases for some of the most common UniqueId types

  type IntId[A] = UniqueId[A,Int]

  type IntIdL[A] = UniqueIdL[A,Int]

  type LongId[A] = UniqueId[A,Long]

  type LongIdL[A] = UniqueIdL[A,Long]

  type StringId[A] = UniqueId[A,String]

  type StringIdL[A] = UniqueIdL[A,String]

  def IntId[A:IntId]: IntId[A] = implicitly

  def LongId[A:LongId]: LongId[A] = implicitly

  def StringId[A:StringId]: StringId[A] = implicitly

  def IntIdL[A:IntIdL]: IntIdL[A] = implicitly

  def LongIdL[A:LongIdL]: LongIdL[A] = implicitly

  def StringIdL[A:StringIdL]: StringIdL[A] = implicitly

  object Shapeless extends std.ShapelessInstances

  object equal {
    def contramap[A:Equal,B](f: B ⇒ A): Equal[B] = Equal equalBy f
  }

  object order {
    def contramap[A:Order,B](f: B ⇒ A): Order[B] = Order orderBy f
  }

  object enum {
    def xmap[A,B](f: A ⇒ B)(g: B ⇒ A)(implicit A: Enum[A]): Enum[B] = new Enum[B] {
      def pred(b: B): B = f(A pred g(b))
      def succ(b: B): B = f(A succ g(b))
      def order(v1: B, v2: B): Ordering = Order[A] order (g(v1), g(v2))
      override def min: Option[B] = A.min map f
      override def max: Option[B] = A.max map f
    }
  }
}

// vim: set ts=2 sw=2 et:
