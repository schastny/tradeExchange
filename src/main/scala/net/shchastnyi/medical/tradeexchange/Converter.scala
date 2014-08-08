package net.shchastnyi.medical.tradeexchange

import java.io.{File, PrintWriter}
import java.text.SimpleDateFormat
import akka.actor.{ActorSystem, Props}
import net.shchastnyi.actors.{ConvertDirectory, DirConverter}

object Converter {

  // Default options
  val ofcDirDefault         = "d:/msconverter"
  val sourceDirDefault      = "d:/tmp/scala"
  val filePrefixDefault     = new SimpleDateFormat("yyyy_MM_dd_").format(new java.util.Date())
  val urlPrefixDefault      = "http://1gb.sebastopol.ua/media/"
  // !Default options

  val helpKey       = "--help"
  val ofcDirKey     = "--ofcDir"
  val sourceDirKey  = "--sourceDir"
  val destDirKey    = "--destDir"
  val filePrefixKey = "--filePrefix"
  val urlPrefixKey  = "--urlPrefix"
  val usage =
    s"Usage: converter " +
    s"[$ofcDirKey ofcDir, default:$ofcDirDefault] " +
    s"[$sourceDirKey sourceFilesDir, default:$sourceDirDefault] " +
    s"[$destDirKey destinationDir, default: $sourceDirDefault/converted] " +
    s"[$filePrefixKey String, default:$filePrefixDefault] " +
    s"[$urlPrefixKey url, default:$urlPrefixDefault]"
  type OptionMap = Map[String, String]

  def main (args: Array[String]) {
    // Parsing command-line args
    val options         = getArgsAsMap(Map(), args.toList)

    val help            = options.getOrElse(helpKey, Nil)
    if (help != Nil) {
      println(usage)
      return
    }

    val ofcDir          = options.getOrElse(ofcDirKey, ofcDirDefault)
    val sourceDir       = options.getOrElse(sourceDirKey, sourceDirDefault)
    val destinationDir  = options.getOrElse(destDirKey, sourceDir + "/converted")
    val filePrefix      = options.getOrElse(filePrefixKey, filePrefixDefault)
    val urlPrefix       = options.getOrElse(urlPrefixKey, urlPrefixDefault)
    // !Parsing command-line args

    // Doing main job
    doTranslit(filePrefix, sourceDir)
    doDocx(ofcDir, sourceDir, destinationDir)
    doPdfLinear(destinationDir)
    //doPdfConcurrent(destinationDir)
    doHtml(destinationDir, urlPrefix)
    // !Doing main job
  }

  /**
   * Parsing command-line args
   * @param map
   * @param list
   * @return
   */
  private def getArgsAsMap(map: OptionMap, list: List[String]): OptionMap = {
    def isSwitch(s : String) = s(0) == '-'
    list match {
      case Nil                                      => map
      case helpKey :: tail                          => getArgsAsMap(map ++ Map(helpKey -> helpKey), tail)
      case ofcDirKey :: value :: tail               => getArgsAsMap(map ++ Map(ofcDirKey -> value), tail)
      case sourceDirKey :: value :: tail            => getArgsAsMap(map ++ Map(sourceDirKey -> value), tail)
      case destDirKey :: value :: tail              => getArgsAsMap(map ++ Map(destDirKey -> value), tail)
      case filePrefixKey :: value :: tail           => getArgsAsMap(map ++ Map(filePrefixKey -> value), tail)
      case urlPrefixKey :: value :: tail            => getArgsAsMap(map ++ Map(urlPrefixKey -> value), tail)
      case string :: opt2 :: tail if isSwitch(opt2) => getArgsAsMap(map ++ Map("infile" -> string), list.tail)
      case string :: Nil                            => getArgsAsMap(map ++ Map("infile" -> string), list.tail)
      case option :: tail                           => println("Unknown option "+option); map
    }
  }

  /**
   * Translate from cyrillic to latin file names
   * @param translitPrefix
   * @param sourceDir
   */
  private def doTranslit(translitPrefix: String, sourceDir: String) {
    new File(sourceDir).listFiles().filter(_.getName.endsWith(".doc")).foreach {
      f => f.renameTo(new File(sourceDir+"/"+translitPrefix+Translit(f.getName)))
    }
  }

  /**
   * Convert from doc to docx
   * @param ofcDir
   * @param sourceDir
   * @param destinationDir
   * @return
   */
  private def doDocx(ofcDir: String, sourceDir: String, destinationDir: String) =
    new DocToDocx(ofcDir, sourceDir, destinationDir).convert

  /**
   * Convert from docx to pdf
   * @param destinationDir
   */
  private def doPdfLinear(destinationDir: String) {
    val pdfConverter = new DocxToPdf
    val docxFiles = new File(destinationDir).listFiles().filter(_.getName.endsWith(".docx"))
    docxFiles foreach { f => pdfConverter.convert(f.getAbsolutePath) }
  }

  /**
   * Warning: Not Safe!
   */
  private def doPdfConcurrent(destinationDir: String) {
    val system = ActorSystem("akka-system")
    val converter = system.actorOf(Props[DirConverter], "mainConverter")
    converter ! ConvertDirectory(destinationDir)
  }

  /**
   * Get titles from pdf documents, construct summary and write it into html file
   * @param destinationDir
   * @param urlPrefix
   */
  private def doHtml(destinationDir: String, urlPrefix: String) = {
    val html = DocParser(destinationDir, urlPrefix)
    val s = new PrintWriter(destinationDir+"/tradingdocs.html")
    s.print(html)
    s.close()
  }

}