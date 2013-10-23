package efa.io

import efa.core._
import EfaIO._
import java.io._
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import scala.xml.PrettyPrinter
import scalaz._, Scalaz._, effect._, iteratee.Iteratee.{sdone, emptyInput}
import scalaz.CharSet.UTF8

case class IOChooser(
  chooser: LogDisIO[JFileChooser],
  adjustPath: String ⇒ String = (s: String) ⇒ s) {
  import IOChooser.Approve

  def saveFile: LogDisIO[Option[File]] = saveConfirm(_ ⇒ IO(true))

  def saveConfirm(confirm: String ⇒ IO[Boolean])
    : LogDisIO[Option[File]] = for {
      c ← chooser
      r ← c.showSaveDialog(null) match {
            case Approve ⇒ {
              val p = adjustPath(c.getSelectedFile.getPath)
              val f = new File(p)
              if (f.exists) liftIO(confirm(p)) map {
                case true ⇒ f.some
                case false ⇒ none[File]
              }
              else f.create map (_.some)
            }
            case _              ⇒ point(none[File])
          }
    } yield r

  def loadFile: LogDisIO[Option[File]] = for {
    c ← chooser
    r ← c.showOpenDialog(null) match {
          case Approve ⇒ point(c.getSelectedFile.some)
          case _       ⇒ point(none[File])
        }
    } yield r

  import AsFile._, AsFile.syntax._

  def lines: EnumIO[String] = iter.optionEnum(loadFile)(_.lines)

  def bytes(buffer: Int): EnumIO[Array[Byte]] = 
    iter.optionEnum(loadFile)(_ bytes buffer)

  def xmlIn[A:ToXml]: EnumIO[A] = iter.optionEnum(loadFile)(_.xmlIn)

  def bytesI: IterIO[Array[Byte],Unit] =
    iter.optionIterM(saveFile)(_.bytesI)

  def linesI(c: CharSet = UTF8): IterIO[String,Unit] =
    iter.optionIterM(saveFile)(_ linesI c)

  def stringI(c: CharSet = UTF8): IterIO[String,Unit] =
    iter.optionIterM(saveFile)(_ stringI c)

  def xmlI[B:TaggedToXml](pretty: Option[PrettyPrinter] = None,
                          c: CharSet = UTF8): IterIO[B,Unit] =
    iter.optionIterM(saveFile)(_ xmlI (pretty, c))

  def xmlTagI[B:ToXml](tag: String,
                 pretty: Option[PrettyPrinter] = None,
                 c: CharSet = UTF8): IterIO[B,Unit] =
    iter.optionIterM(saveFile)(_ xmlTagI (tag, pretty, c))
}

object IOChooser {
  val Approve = JFileChooser.APPROVE_OPTION

  val noFilter: IOChooser = IOChooser(point(new JFileChooser))

  /** A file chooser that filters files according to their
    * extension.
    *
    * If the list of possible extensions is not empty and
    * the file selected by the user does not end on one
    * of the extensions in the list, the first extension
    * in the list is automatically appended to the file
    * name.
    */
  def filter(
    desc: String,
    selected: Option[String],
    exts: String*
  ): IOChooser = IOChooser (
    point {
      val c = new JFileChooser
      if (exts.nonEmpty)
        c.setFileFilter(new FileNameExtensionFilter(desc, exts: _*))

      selected foreach {
        new java.io.File(_) match {
          case f if (f.exists && f.isDirectory) ⇒ c.setCurrentDirectory(f)
          case f if (f.exists)                  ⇒ c.setSelectedFile(f)
          case _                                ⇒ 
        }
      }

      c
    },
    adjustEnding(exts)
  )

  private[io] def adjustEnding(exts: Seq[String])(p: String): String =
    exts.headOption cata (
      h ⇒ exts find { e ⇒ p endsWith s".$e" } cata (_ ⇒ p, s"$p.$h"),
      p
    )

  def txtOnly(selected: Option[String]): IOChooser = 
    filter(loc.txtFiles, selected, loc.txtExt)

  def all(selected: Option[String]): IOChooser = 
    filter(loc.allFiles, selected)

  def folders(selected: Option[String]): IOChooser = IOChooser (
    point {
      val c = new JFileChooser

      selected foreach {
        new java.io.File(_) match {
          case f if (f.exists && f.isDirectory) ⇒ c.setCurrentDirectory(f)
          case _                                ⇒ 
        }
      }

      c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)

      c
    }
  )
}

// vim: set ts=2 sw=2 et:
