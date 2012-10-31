package efa.core

//import scalaz._; import Scalaz._
//import org.scalacheck._
//
//object ValidatorTTest
//extends Properties("ValidatorT")
//with ValidatorTFunctions 
//with IdentityPimps {
//
//  type IdVal[R,A] = ValidatorT[Identity,R,A]
//
//  //needed to make sure that pimp methods of MA are available for IdVal
//  implicit def ToMA[R,A] (i: IdVal[R,A]) = ValidatorT2MA(i)
//  implicit def IdValFunctor[R]: Functor[({type l[a] = IdVal[R,a]})#l] =
//    ValidatorTFunctor
//  implicit def IdValApply[R]: Apply[({type l[a] = IdVal[R,a]})#l] = ValidatorTApply
//  implicit def IdValPure[R]: Pure[({type l[a] = IdVal[R,a]})#l] = ValidatorTPure
//  implicit def IdValContravariant[A]: Contravariant[({type l[a] = IdVal[a,A]})#l] =
//    ValidatorTContravariant
//
//  val validator = Validators.interval(0,100)
//  val idVal: IdVal[Int,Int] = ValidatorT(validator)
//
//  property("apply") = Prop.forAll { i: Int ⇒ 
//    validator(i) ≟ idVal(i).value
//  }
//
//  property("map") = Prop.forAll { i: Int ⇒ 
//    val f = (n: Int) ⇒ n.toString.reverse
//    (validator(i) map f) ≟ ((idVal ∘ f) apply i value)
//  }
//
//  property("applicative") = Prop.forAll { n: Int ⇒ 
//    val validator2 = Validators.interval(-1000, 10)
//    val idVal2: IdVal[Int,Int] = ValidatorT(validator2)
//    val combo = idVal ⊛ idVal2 apply (_ + _)
//    val comboVal = (i: Int) ⇒ validator(i) ⊛ validator2(i) apply (_ + _)
//    comboVal(n) ≟ (combo apply n value) 
//  }
//
//  property("pure") = Prop.forAll { p: (Int,Int) ⇒ 
//    val (a,b) = p
//    val aId: IdVal[Int,Int] = a.η[({type l[a] = IdVal[Int,a]})#l]
//    (aId apply b value) ≟ a.successR
//  }
//
//  property("contramap") = Prop.forAll { i: Int ⇒ 
//    val cm = idVal contramap { s: String ⇒ s.toInt }
//    (cm apply i.toString value) ≟ validator(i)
//  }
//
//  property("andThen") = Prop.forAll { n: Int ⇒
//    val validator2 = Validators.interval(-1000, 10)
//    val andThen = idVal andThen validator2
//    (andThen apply n value) ≟ (idVal(n).value flatMap validator2)
//  }
//
//  property("lensed") = Prop.forAll { p: (String,Int) ⇒ 
//    val lensed = idVal lensed Lens.snd[String,Int]
//    (lensed(p).value map (_._2)) ≟ idVal(p._2).value
//  }
//      
//}

// vim: set ts=2 sw=2 et:
