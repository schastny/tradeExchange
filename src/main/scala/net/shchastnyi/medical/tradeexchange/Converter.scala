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
    f:File => f.renameTo(new File(sourceDir+"/"+Translit(f.getName)))
  }

  //doc -> docx
  val docConverter = new DocToDocx(ofcDir, sourceDir, destinationDir)
  val step1Result = docConverter.convert

  //docx -> pdf
  if (step1Result == 0) {
    val doccer = new DocxToPdf
    val docxFiles = new File(destinationDir).listFiles().filter(_.getName.endsWith(".docx"))
//    docxFiles foreach { f:File => doccer.convert(f.getAbsolutePath) }
  }

  //parsing files
  val folder = """d:\\tmp\\scala\\converted"""
  val file = "obyyavlenie_o_nachale_torgov_ovoshi_konservir.docx.pdf"
  val xx = folder +"\\"+ file
  val pdf = new PdfReader
  pdf.read(xx)
}