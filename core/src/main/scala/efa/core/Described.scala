package efa.core

import scalaz.Contravariant

/** A type class representing a short description associated with a type.
  *
  * Typically, these descriptions can be displayed conveniently in GUIs.
  */
trait Described[-A] { self ⇒ 
  def shortDesc (a: A): String
}

/** This version of Described uses html to format its descriptions.
  *
  * The html-description consists of a bold title (the object's name)
  * plus several tags in bold, each on its own line.
  */
trait HtmlDescribed[A] extends Described[A] with Named[A] { self ⇒ 
  import Described.{Tag, Tags}

  /** Returns a list of html tag in the form of `(String,String)`-pairs.
    */
  def tags (a: A): Tags

  override def shortDesc (a: A): String =
    Described namePlusTags (name (a), tags (a): _*)
}

/** Helper functions to create typical html-formatted desciptions.
  */
trait DescribedFunctions {
  import Described.{Tag, Tags}

  def contramap[A,B](d: Described[A])(f: B ⇒ A): Described[B] =
    new Described[B] {
      def shortDesc(b: B) = d shortDesc f(b)
    }

  /** Displays a `Tag` on its own line, starting with the
    * `Tag`'s name in bold, followed by its value.
    */
  def formatTag(t: Tag): String =  s"<P><B>${t._1}</B>: ${t._2}</P>"

  /** Displays a list of `Tag`s each on a separate line.
    */
  def formatTags(ts: Tag*): String =  ts map formatTag mkString ""

  /** Displays a header with a name plus a list of `Tag`s (each on
    * a separate line). The whole string is wrapped in `html`
    * start and end tags.
    */
  def namePlusTags(n: String, ts: Tag*): String =
    titleBodyHtml(n, formatTags (ts: _*))

  /** Displays a header with a title in bold followed by a body
    * in a new paragraph.
    */
  def titleBody(title: String, body: String) = s"<P><B>$title</B></P>$body"

  /** Displays a header with a title in bold followed by a body
    * in a new paragraph. The whole string is wrapped in `html`
    * start and end tags.
    */
  def titleBodyHtml(title: String, body: String) = 
    wrapHtml(titleBody (title, body))

  /** Wraps a string in `html` start and end tags.
    */
  def wrapHtml(s: String): String = s"<html>$s</html>"
}

object Described extends DescribedFunctions {
  @inline def apply[A:Described]: Described[A] = implicitly

  /** Represents a single property (its name and a short description)
    *
    * Such tags are used to display the names and values of properties
    * conveniently in tooltip texts for instance.
    */
  type Tag = (String, String)

  type Tags = Vector[Tag]

  implicit val DescribedContravariant: Contravariant[Described] =
    new Contravariant[Described] {
      override def contramap[A,B](d: Described[A])(f: B ⇒ A) =
        Described.contramap(d)(f)
    }
}

object HtmlDescribed {
  @inline def apply[A:HtmlDescribed]: HtmlDescribed[A] = implicitly

  def contramap[A,B](d: HtmlDescribed[A])(f: B ⇒ A): HtmlDescribed[B] =
    new HtmlDescribed[B] {
      def tags(b: B) = d tags f(b)
      def name(b: B) = d name f(b)
    }

  implicit val HtmlDescribedContravariant: Contravariant[HtmlDescribed] =
    new Contravariant[HtmlDescribed] {
      override def contramap[A,B](d: HtmlDescribed[A])(f: B ⇒ A) =
        HtmlDescribed.contramap(d)(f)
    }
}

// vim: set ts=2 sw=2 et:
