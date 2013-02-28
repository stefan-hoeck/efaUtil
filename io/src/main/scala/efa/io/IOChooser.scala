package efa.io

import scalaz._, Scalaz._, effect._
import efa.core._
import java.io._
import javax.swing.filechooser.FileNameExtensionFilter
import scala.swing.FileChooser
import scala.swing.FileChooser.Result
import valLogIO._

case class IOChooser(chooser: ValLogIO[FileChooser]) {
  def saveFile: ValLogIO[Option[File]] = for {
    c ← chooser
    r ←  c.showSaveDialog(null) match {
           case Result.Approve ⇒ AsFile[File] create c.selectedFile map (_.some)
           case _ ⇒ none[File].η[ValLogIO]
         }
  } yield r

  def loadFile: ValLogIO[Option[File]] = for {
    c ← chooser
    r ← point (
          c.showOpenDialog(null) match {
            case Result.Approve ⇒ c.selectedFile.some
            case _ ⇒ none[File]
          }
        )
  } yield r
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
