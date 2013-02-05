package efa.core

/**
  * A type class representing a short description associated with a type.
  *
  * Typically, these descriptions can be displayed conveniently in GUIs.
  *
  */
trait Described[-A] {
  def shortDesc (a: A): String
}

/**
  * This version of Described uses html to format its descriptions.
  *
  * The html-description consists of a bold title (the object's name)
  * plus several tags in bold, each on its own line.
  *
  */
trait HtmlDescribed[A] extends Described[A] with Named[A] {
  import Described.{Tag, Tags}

  def tags (a: A): Tags

  override def shortDesc (a: A): String =
    Described namePlusTags (name (a), tags (a): _*)
}

trait DescribedFunctions {
  import Described.{Tag, Tags}

  def formatTag (t: Tag): String =  s"<P><B>${t._1}</B>: ${t._2}</P>"

  def formatTags (ts: Tag*): String =  ts map formatTag mkString ""

  def namePlusTags (n: String, ts: Tag*): String =
    titleBodyHtml (n, formatTags (ts: _*))

  def titleBody (title: String, body: String) = s"<P><B>$title</B></P>$body"

  def titleBodyHtml (title: String, body: String) = 
    wrapHtml (titleBody (title, body))

  def wrapHtml (s: String): String = s"<html>$s</html>"
}

object Described extends DescribedFunctions {
  @inline def apply[A:Described]: Described[A] = implicitly

  type Tag = (String, String)

  type Tags = IndexedSeq[Tag]
}

// vim: set ts=2 sw=2 et:
