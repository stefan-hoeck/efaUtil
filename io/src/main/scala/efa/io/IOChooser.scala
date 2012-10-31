package efa.io

import scalaz._, Scalaz._, effect._
import efa.core._
import java.io.{IOException, OutputStream, InputStream,
  Writer, OutputStreamWriter, File, FileOutputStream, FileInputStream}
import javax.swing.filechooser.FileNameExtensionFilter
import scala.swing.FileChooser
import scala.swing.FileChooser.Result
import valLogIO._, FileIO._

case class IOChooser (chooser: ValLogIO[FileChooser]) {
  def saveFile: ValLogIO[Option[File]] = for {
    c ← chooser
    r ←  c.showSaveDialog(null) match {
           case Result.Approve ⇒ createFile(c.selectedFile) ∘ (_.some)
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

  def withWriter (cs: CharSet)(f: Writer ⇒ ValLogIO[Unit]): ValLogIO[Unit] =
    saveFile ∗ ( _ fold (fi ⇒ FileIO.withWriter(fi, cs)(f), ().η[ValLogIO]) )
  
  def withOutputStream (f: OutputStream ⇒ ValLogIO[Unit]): ValLogIO[Unit] =
    saveFile ∗ ( _ fold (fi ⇒ FileIO.withOutputStream(fi)(f), ().η[ValLogIO]) )

}

object IOChooser {
  val noFilter: IOChooser = IOChooser(point(new FileChooser))

  def filter(desc: String, exts: String*): IOChooser = IOChooser (
    point(new FileChooser {
      fileFilter = new FileNameExtensionFilter (desc, exts: _*)
    })
  )

  def selectedFile(path: String): IOChooser = IOChooser(point(new FileChooser{
    selectedFile = new java.io.File(path) match {
      case f if (f.exists) ⇒ f
      case _               ⇒ selectedFile
    }
  }))
}

// vim: set ts=2 sw=2 et:
