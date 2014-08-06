package efa.core

import language.experimental.macros
import scala.reflect.macros.blackbox.Context
import scala.reflect.ClassTag

/* Type class for scala types that are unaffected by erasure.
 *
 * This is used for instance to guarantee type safety in
 * Netbeans lookups when used from scala code.
 *
 * Instances of this class are usually inferred on their own, so
 * you do not need to create them manually. A macro checks that
 * one does not try to add or retrieve objects from a lookup
 * with a class that is affected by erasure.
 */
sealed trait Unerased[A] {
  def clazz: Class[A]
}

object Unerased {
  /* Retruns an instance of Unerased[A] if available */
  def apply[A:Unerased]: Unerased[A] = implicitly

  /* Automatically creates instance of Unerased[A]
   * if A is a type that is not affected by erasure.
   */
  implicit def instance[A:ClassTag]: Unerased[A] = macro impl[A]

  def impl[A:c.WeakTypeTag](c: Context)(ct: c.Expr[ClassTag[A]])
    : c.Expr[Unerased[A]] = {
    import c.universe._

    val tpe: Type = weakTypeTag[A].tpe

    if (tpe =:= tpe.erasure)
      c.Expr[Unerased[A]](q"""efa.core.Unerased.unsafe($ct)""")
    else
      c.abort(
        c.enclosingPosition, 
        s"Type ${tpe.typeSymbol.name} is modified by type erasure. " +
        "It is therefore unsafe to use this type in Lookups"
      )
  }

  /* Do NOT call from within your code */
  def unsafe[A](cl: ClassTag[A]): Unerased[A] = new Unerased[A] {
    def clazz = cl.runtimeClass.asInstanceOf[Class[A]]
  }
}

// vim: set ts=2 sw=2 et:
