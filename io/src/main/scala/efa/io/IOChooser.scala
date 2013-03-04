package efa.io

import scalaz._, Scalaz._, effect._, iteratee.Iteratee.{sdone, emptyInput}
import efa.core._
import java.io._
import javax.swing.filechooser.FileNameExtensionFilter
import scala.swing.FileChooser
import scala.swing.FileChooser.Result
import logDisIO._

case class IOChooser(chooser: LogDisIO[FileChooser]) {
  def saveFile: LogDisIO[Option[File]] = for {
    c ← chooser
    r ← c.showSaveDialog(null) match {
          case Result.Approve ⇒ AsFile[File] create c.selectedFile map (_.some)
          case _ ⇒ none[File].η[LogDisIO]
        }
  } yield r

  def loadFile: LogDisIO[Option[File]] = for {
    c ← chooser
    r ← point (
          c.showOpenDialog(null) match {
            case Result.Approve ⇒ c.selectedFile.some
            case _ ⇒ none[File]
          }
        )
    } yield r

  import AsFile._, AsFile.syntax._

  def lines: EnumIO[String] = iter.optionEnum(loadFile)(_.lines)

  def bytes(buffer: Int): EnumIO[Array[Byte]] = 
    iter.optionEnum(loadFile)(_ bytes buffer)

  def xml[A:ToXml]: EnumIO[A] = iter.optionEnum(loadFile)(_.xmlIn)
}

object IOChooser {
  val noFilter: IOChooser = IOChooser(point(new FileChooser))

  def filter(
    desc: String,
    selected: Option[String],
    exts: String*
  ): IOChooser = IOChooser (
    point(
      new FileChooser {
        if (exts.nonEmpty)
          fileFilter = new FileNameExtensionFilter (desc, exts: _*)

        selected foreach {
          new java.io.File(_) match {
            case f if (f.exists && f.isDirectory) ⇒ peer.setCurrentDirectory(f)
            case f if (f.exists)                  ⇒ selectedFile = f
            case _                                ⇒ 
          }
        }
      }
    )
  )

  def txtOnly (selected: Option[String]): IOChooser = 
    filter(loc.txtFiles, selected, loc.txtExt)

  def all (selected: Option[String]): IOChooser = 
    filter(loc.allFiles, selected)

  def folders (selected: Option[String]): IOChooser = IOChooser (
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
