package net.shchastnyi.medical.tradeexchange

object Converter extends App {

  val docConverter = new DocToDocx("d:/msconverter/", "d:/tmp/scala", "d:/tmp/scala/converted")
  docConverter.setFoldersToConvert
//  docConverter.importFile("[Run]", "OLOLO")
//  val step1Result = docConverter.convert

//  if (step1Result == 0) {
//    val doccer = new DocxToPdf
//    doccer.convert("d:/tmp/test.docx")
//  }

}