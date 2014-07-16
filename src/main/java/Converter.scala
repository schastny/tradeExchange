package net.shchastnyi.medical.tradeexchange

object Converter extends App {

  val doccer = new DocToPdf
  doccer.convert("""d:/tmp/test.docx""")

}