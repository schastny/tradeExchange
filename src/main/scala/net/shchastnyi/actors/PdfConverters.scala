package net.shchastnyi.actors

import java.io.File

import akka.actor._
import net.shchastnyi.actors.Reaper.WatchMe
import net.shchastnyi.medical.tradeexchange.DocxToPdf

case class ConvertDirectory(absolutePathToDir: String)
case class ConvertFile(absolutePathToFile: String)

class DirConverter extends Actor {
  override def receive: Receive = {
    case ConvertDirectory(absolutePathToDir) => {
      val reaper = context.actorOf(Props[ProductionReaper])
      val docxFiles = new File(absolutePathToDir).listFiles().filter(_.getName.endsWith(".docx"))
      docxFiles foreach {
        f =>
          val slaveConverter = context.actorOf(Props[FileConverter])
          reaper ! WatchMe(slaveConverter)
          slaveConverter ! ConvertFile(f.getAbsolutePath)
      }
    }
  }
}

class FileConverter extends Actor {
  override def receive: Actor.Receive = {
    case ConvertFile(absolutePathToFile) => {
      val pdfConverter = new DocxToPdf
      pdfConverter.convert(absolutePathToFile)
      context.stop(self)
    }
  }
}