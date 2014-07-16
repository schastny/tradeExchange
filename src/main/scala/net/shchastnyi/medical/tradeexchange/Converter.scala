package net.shchastnyi.medical.tradeexchange

object Converter extends App {

  val destinationDir = """d:\\tmp\\scala\\converted""";

  val docConverter = new DocToDocx("d:/msconverter/", """d:\\tmp\\scala""", destinationDir)
  docConverter.setFoldersToConvert
  docConverter.setDestinationPathTemplate
  val step1Result = docConverter.convert

  if (step1Result == 0) {
    val doccer = new DocxToPdf
    doccer.convert(destinationDir+"\\test.docx")
  }

}