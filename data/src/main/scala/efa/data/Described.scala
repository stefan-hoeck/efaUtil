package efa.data

trait Described[-A] {
  def desc (a: A): String
}

trait HtmlDescribed[A] extends Described[A] with Named[A] {
  import Described.{Tag, Tags, B, Be, Br, Html, HtmlE}

  def tags (a: A): Tags

  override def desc (a: A): String = {
    val title = s"$B${name (a)}$Be"
    def tag (t: Tag): String = s"$B${t._1}$Be: ${t._2}"
    val ts = tags (a) map tag mkString Br

    s"$Html$title$Br$ts$HtmlE"
  }
}

object Described {
  @inline def apply[A:Described]: Described[A] = implicitly

  type Tag = (String, String)

  type Tags = IndexedSeq[Tag]

  final val B = "<b>"
  final val Be = "</b>"
  final val Br = "<br>"
  final val Html = "<html>"
  final val HtmlE = "</html>"
}

// vim: set ts=2 sw=2 et:
