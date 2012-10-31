package efa.core

//import scalaz._, Scalaz._
//import org.scalacheck._
//
//object ValResTTest extends Properties("ValResT") with IdentityPimps {
//  import ValResT._
//
//  type IdVal[A] = ValResT[Identity,A]
//
//  //needed to make sure that pimp methods of MA are available for IdVal
//  implicit def ToMA[A] (i: IdVal[A]) = ValResT2MA(i)
//  implicit def IdValFunctor: Functor[IdVal] = ValResTFunctor
//  implicit def IdValApply: Apply[IdVal] = ValResTApply
//  implicit def IdValPure: Pure[IdVal] = ValResTPure
//
//  def validator = Validators.interval(0,100)
//  def idVal (i: Int): IdVal[Int] = ValResT (validator (i))
//
//  property("map") = Prop.forAll { i: Int ⇒ 
//    val f = (n: Int) ⇒ n.toString.reverse
//    (validator(i) map f) ≟ (idVal (i) ∘ f value)
//  }
//
//  property("mapTest") = {
//    val i = 111239871
//    val f = (n: Int) ⇒ n.toString.reverse
//    (validator(i) map f) ≟ (idVal (i) ∘ f value)
//  }
//
//  property("applicative") = Prop.forAll { n: Int ⇒ 
//    val validator2 = Validators.interval(-1000, 10)
//    val idVal2: IdVal[Int] = ValResT(validator2(n))
//    val combo = idVal(n) ⊛ idVal2 apply (_ + _)
//    val comboVal = validator(n) ⊛ validator2(n) apply (_ + _)
//    comboVal ≟ combo.value 
//  }
//
//  property("pure") = Prop.forAll { i: Int ⇒ 
//    i.η[IdVal].value ≟ i.successR
//  }
//      
//}

// vim: set ts=2 sw=2 et:
