package net.shchastnyi.medical.tradeexchange

import java.io.{BufferedReader, File, FileInputStream}

import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.pdf.PDFParser
import org.apache.tika.parser.{ParseContext, ParsingReader}

import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

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

  val zapros_results_pattern     = """(?s)(ОБЪЯВЛЕНИЕ).*?(о результатах проведения процедуры запроса ценовых предложений)""".r
  val zapros_results_title       = "Результаты запроса ценовых предложений"

  val dkt_pattern         = """(?s)ДОКУМЕНТАЦИЯ КОНКУРСНЫХ ТОРГОВ""".r
  val dkt_title           = "Документация конкурсных торгов"

  val uvedoml_otmena_pattern      = """(?s)(Уведомление).*?(про отмену торгов)""".r
  val uvedoml_otmena_title        = "Уведомление про отмену торгов"

  val uvedoml_accept_pattern      = """(?s)(УВЕДОМЛЕНИЕ).*?(об акцепте)""".r
  val uvedoml_accept_title = "Уведомление об акцепте предложения к онкурсных торгов"

  val quotesPattern       = """(?<=«).*?(?=»)""".r
  val misc_title          = "Тендерная документация"

  def apply(pathToFiles: String, urlPrefix: String): String = prepareHtmlList(pathToFiles, urlPrefix)


  /**
   * Given the list of pdf/docx files, construct an html text with its titles and download links
   * @param pathToFiles
   * @return
   */
  def prepareHtmlList(pathToFiles: String, urlPrefix: String): String = {
    constructMap(pathToFiles).map(
      tuple => {
        String.format("<li>%s <a href='%s%s'>Скачать в формате pdf</a></li>", tuple._2, urlPrefix, tuple._1)
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
    val lines = readFileInArray(filePath).mkString(" ")

    def findTitle(documentPattern: Regex, lines: String) = {
      (documentTitle: String) => {
        val line = documentPattern.findFirstIn(lines)
        val documentSubtitle = quotesPattern.findFirstIn(line.getOrElse("N/A"))
        String.format("%s (%s)", documentTitle, documentSubtitle.getOrElse(""))
      }
    }

    val patternForTwo   = """(?s)(?<=5\.1\.).*?(»)""".r
    val patternResult   = """(?s)(?<=3\.1\.).*?(»)""".r
    val patternDkt      = """(?s)(?<=открытых торгов по закупке:).*?(»)""".r
    val patternOtmena   = """(?s)(?<=2\.1\.).*?(»)""".r
    val patternAccept   = """(?s)(?<=2\.1\.).*?(»)""".r
    val findTitleForTwo = findTitle(patternForTwo, lines)
    val findTitleResult = findTitle(patternResult, lines)(zapros_results_title)
    val findTitleDkt    = findTitle(patternDkt, lines)(dkt_title)
    val findTitleOtmena = findTitle(patternOtmena, lines)(uvedoml_otmena_title)
    val findTitleAccept = findTitle(patternAccept, lines)(uvedoml_accept_title)

    if ( plan_pattern.findFirstIn(lines).nonEmpty )
      plan_title
    else if ( provedenie_pattern.findFirstIn(lines).nonEmpty )
      findTitleForTwo(provedenie_title)
    else if ( zapros_pattern.findFirstIn(lines).nonEmpty )
      findTitleForTwo(zapros_title)
    else if ( zapros_results_pattern.findFirstIn(lines).nonEmpty )
      findTitleResult
    else if ( dkt_pattern.findFirstIn(lines).nonEmpty )
      findTitleDkt
    else if ( uvedoml_otmena_pattern.findFirstIn(lines).nonEmpty )
      findTitleOtmena
    else if ( uvedoml_accept_pattern.findFirstIn(lines).nonEmpty )
      findTitleAccept
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
    val buffReader = new BufferedReader(parsReader)
    val textLines = ArrayBuffer.empty[String]
    Stream.continually(buffReader.readLine()).takeWhile(_ != null).foreach(textLines+=_)
    stream.close()
    textLines.map(_.trim).filter(!_.isEmpty).toArray
  }

}
