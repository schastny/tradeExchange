package net.shchastnyi.medical.tradeexchange

import java.io.{FileInputStream, File}

import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.{ParsingReader, ParseContext}
import org.apache.tika.parser.pdf.PDFParser

import scala.util.matching.Regex
import scala.util.parsing.input.StreamReader

/**
 * Given the list of pdf/docx files, construct an html text with its titles and download links
 */
object DocParser {

  val plan_pattern        = """(?s)(ГОДОВОЙ ПЛАН ЗАКУПОК).*?(на)""".r
  val plan_title          = "Годовой план закупок"
  val provedenie_pattern  = """(?s)(Объявление).*?(о проведении открытых торгов)""".r
  val provedenie_title    = "Объявление о проведении открытых торгов"
  val zapros_pattern      = """(?s)(ЗАПРОС).*?(ценовых предложений)""".r
  val zapros_title        = "Запрос ценовых предложений"
  val dkt_pattern         = """(?s)ДОКУМЕНТАЦИЯ КОНКУРСНЫХ ТОРГОВ""".r
  val dkt_title           = "Документация конкурсных торгов"

  val quotesPattern       = """(?<=«).*?(?=»)""".r
  val misc_title          = "Тендерная документация"

  def apply(pathToFiles: String): String = {
    prepareHtmlList(pathToFiles)
  }

  /**
   * Given the list of pdf/docx files, construct an html text with its titles and download links
   * @param pathToFiles
   * @return
   */
  def prepareHtmlList(pathToFiles: String): String = {
    constructMap(pathToFiles).map(
      tuple => {
        String.format("<li>%s <a href='%s'>Скачать в формате pdf</a></li>", tuple._2, tuple._1)
      }
    ).mkString("\n")
  }

  /**
   * Make map [Filename, Document Title]
   * @param pathToFiles
   * @return
   */
  def constructMap(pathToFiles: String): Map[String, String] = {
    val docxFiles = new File(pathToFiles).listFiles().filter(_.getName.endsWith(".pdf"))
    docxFiles.map{
      f => (f.getName, DocParser.constructTitleForDocument(f.getAbsolutePath))
    }.toMap
  }

  /**
   * Parse content of the given document and suggest some title for the document
   * @param filePath
   * @return
   */
  private def constructTitleForDocument(filePath: String): String = {
    val lines = readFileInArray(filePath).mkString

    def findTitle(documentPattern: Regex, lines: String) = {
      (documentTitle: String) => {
        val line = documentPattern.findFirstIn(lines.mkString)
        val documentSubtitle = quotesPattern.findFirstIn(line.getOrElse("N/A"))
        String.format("%s (%s)", documentTitle, documentSubtitle.getOrElse(""))
      }
    }
    val patternForTwo = """(?s)(?<=5\.1\.).*?(»)""".r
    val patternDkt = """(?s)(?<=открытых торгов по закупке:).*?(»)""".r
    val findTitleForTwo = findTitle(patternForTwo, lines)
    val findTitleDkt = findTitle(patternDkt, lines)

    if ( !plan_pattern.findFirstIn(lines).isEmpty )
      plan_title
    else if ( !provedenie_pattern.findFirstIn(lines).isEmpty )
      findTitleForTwo(provedenie_title)
    else if ( !zapros_pattern.findFirstIn(lines).isEmpty )
      findTitleForTwo(zapros_title)
    else if ( !dkt_pattern.findFirstIn(lines).isEmpty )
      findTitleDkt(dkt_title)
    else
      misc_title
  }

  /**
   * Read pdf into Array[String]
   * @param filePath full path to a file
   * @return Just lines which contain some text
   */
  private def readFileInArray(filePath: String): Array[String] = {
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
