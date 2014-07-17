package net.shchastnyi.medical.tradeexchange

import java.io.{FileInputStream, File}

import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.{ParsingReader, ParseContext}
import org.apache.tika.parser.pdf.PDFParser

import scala.util.parsing.input.StreamReader

/**
 * Given the list of pdf/docx files, construct an html text with its titles and download links
 */
object FileLister {

  val plan = """(?s)(ГОДОВОЙ ПЛАН ЗАКУПОК).*?(на)""".r
  val provedenie = """(?s)(Объявление).*?(о проведении открытых торгов)""".r
  val zapros = """(?s)(ЗАПРОС).*?(ценовых предложений)""".r
  val dkt = """(?s)ДОКУМЕНТАЦИЯ КОНКУРСНЫХ ТОРГОВ""".r

  def apply(pathToFiles: String): String = {
    val docxFiles = (new File(pathToFiles)).listFiles().filter(_.getName.endsWith(".docx"))
    ""
  }

  def constructMap(pathToFiles: String): Map[String, String] = {
    val docxFiles = new File(pathToFiles).listFiles().filter(_.getName.endsWith(".pdf"))
    docxFiles.map{
      f => (f.getName, FileLister.getNameForFile(f.getAbsolutePath))
    }.toMap
  }

  def getNameForFile(filePath: String): String = {
    val lines = readFileToString(filePath).mkString
    if ( !plan.findFirstIn(lines).isEmpty )
      "Годовой план закупок"
    else if ( !provedenie.findFirstIn(lines).isEmpty )
      "Объявление о проведении открытых торгов"
    else if ( !zapros.findFirstIn(lines).isEmpty )
      "Запрос ценовых предложений"
    else
      "Документация конкурсных торгов"
  }

  /**
   * Read pdf into Array[String]
   * @param filePath full path to a file
   * @return Just lines which contain some text
   */
  def readFileToString(filePath: String): Array[String] = {
    val pdfParser = new PDFParser()
    val stream = new FileInputStream(filePath)
    val metadata = new Metadata()
    val context = new ParseContext()

    val parsReader = new ParsingReader(pdfParser, stream, metadata, context)
    val textLines = StreamReader(parsReader).source
    stream.close()
    textLines.toString.split("\n").map(_.trim).filter(!_.isEmpty)
  }

}
