package efa.io

import efa.core._
import EfaIO._
import java.io._
import javax.swing.filechooser.FileNameExtensionFilter
import scala.swing.FileChooser, FileChooser.Result
import scala.xml.PrettyPrinter
import scalaz._, Scalaz._, effect._, iteratee.Iteratee.{sdone, emptyInput}
import scalaz.CharSet.UTF8

case class IOChooser(
  chooser: LogDisIO[FileChooser],
  adjustPath: String ⇒ String = (s: String) ⇒ s) {
  def saveFile: LogDisIO[Option[File]] = for {
    c ← chooser
    r ← c.showSaveDialog(null) match {
          case Result.Approve ⇒ 
            adjustPath(c.selectedFile.getPath).create map (_.some)
          case _              ⇒ point(none[File])
        }
  } yield r

  def loadFile: LogDisIO[Option[File]] = for {
    c ← chooser
    r ← c.showOpenDialog(null) match {
          case Result.Approve ⇒ point(c.selectedFile.some)
          case _              ⇒ point(none[File])
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
  val noFilter: IOChooser = IOChooser(point(new FileChooser))

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
    point(
      new FileChooser {
        if (exts.nonEmpty)
          fileFilter = new FileNameExtensionFilter(desc, exts: _*)

        selected foreach {
          new java.io.File(_) match {
            case f if (f.exists && f.isDirectory) ⇒ peer.setCurrentDirectory(f)
            case f if (f.exists)                  ⇒ selectedFile = f
            case _                                ⇒ 
          }
        }
      }
    ),
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
    point(
      new FileChooser {
        selected foreach {
          new java.io.File(_) match {
            case f if (f.exists && f.isDirectory) ⇒ peer.setCurrentDirectory(f)
            case _                                ⇒ 
          }
        }

        fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly
      }
    )
  )
}

// vim: set ts=2 sw=2 et:
