package net.shchastnyi.medical.tradeexchange

import java.io.File

object Converter extends App {

  //Constants
  val ofcDir = "d:/msconverter/"
  val sourceDir = """d:\\tmp\\scala"""
  val destinationDir = """d:\\tmp\\scala\\converted"""

  //Making translit
  val docFiles = new File(sourceDir).listFiles().filter(_.getName.endsWith(".doc"))
  docFiles foreach {
    f => f.renameTo(new File(sourceDir+"/"+Translit(f.getName)))
  }

  //doc -> docx
  val docConverter = new DocToDocx(ofcDir, sourceDir, destinationDir)
  val step1Result = docConverter.convert

  //docx -> pdf
  if (step1Result == 0) {
    val pdfConverter = new DocxToPdf
    val docxFiles = new File(destinationDir).listFiles().filter(_.getName.endsWith(".docx"))
    docxFiles foreach { f => pdfConverter.convert(f.getAbsolutePath) }
  }

  //generating html
  val html = DocParser(destinationDir)
  println(html)

}