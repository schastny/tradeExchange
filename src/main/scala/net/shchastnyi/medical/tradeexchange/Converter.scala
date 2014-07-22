package net.shchastnyi.medical.tradeexchange

import java.io.File

import akka.actor.{Props, ActorSystem}
import net.shchastnyi.actors.{ConvertDirectory, DirConverter}

object Converter {

  val ofcDir = "d:/msconverter/"
  val sourceDir = """d:\\tmp\\scala"""
  val destinationDir = """d:\\tmp\\scala\\converted"""
  val translitPrefix = "2014_07_14_"
  val urlPrefix = "http://1gb.sebastopol.ua/media/"

  def main (args: Array[String]) {
    doTranslit
    doDocx
    doPdfLinear
//    doPdfConcurrent
    println(doHtml)
  }

  def doTranslit {
    new File(sourceDir).listFiles().filter(_.getName.endsWith(".doc")).foreach {
      f => f.renameTo(new File(sourceDir+"/"+translitPrefix+Translit(f.getName)))
    }
  }

  def doDocx = new DocToDocx(ofcDir, sourceDir, destinationDir).convert


  def doPdfLinear {
    val pdfConverter = new DocxToPdf
    val docxFiles = new File(destinationDir).listFiles().filter(_.getName.endsWith(".docx"))
    docxFiles foreach { f => pdfConverter.convert(f.getAbsolutePath) }
  }

  /**
   * Warning: Not Safe!
   */
  def doPdfConcurrent {
    val system = ActorSystem("akka-system")
    val converter = system.actorOf(Props[DirConverter], "mainConverter")
    converter ! ConvertDirectory(destinationDir)
  }

  def doHtml = DocParser(destinationDir, urlPrefix)

}