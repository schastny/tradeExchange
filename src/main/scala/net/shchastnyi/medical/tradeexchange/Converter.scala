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
  val debugKey      = "--debug"
  val ofcDirKey     = "--ofcDir"
  val sourceDirKey  = "--sourceDir"
  val destDirKey    = "--destDir"
  val filePrefixKey = "--filePrefix"
  val urlPrefixKey  = "--urlPrefix"
  val usage =
    s"Usage: converter \n" +
    s"[$debugKey print options applied] \n" +
    s"[$ofcDirKey ofcDir, default: $ofcDirDefault] \n" +
    s"[$sourceDirKey sourceFilesDir, default: $sourceDirDefault] \n" +
    s"[$destDirKey destinationDir, default: $sourceDirDefault/converted] \n" +
    s"[$filePrefixKey String, default: $filePrefixDefault] \n" +
    s"[$urlPrefixKey url, default: $urlPrefixDefault]"
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

    // Printing arguments
    if (options.get(debugKey) != None) {
      val arguments =
        s"Applying arguments: \n" +
          s"$ofcDirKey: $ofcDir \n" +
          s"$sourceDirKey: $sourceDir \n" +
          s"$destDirKey: $destinationDir \n" +
          s"$filePrefixKey: $filePrefix \n" +
          s"$urlPrefixKey: $urlPrefix"
      println(arguments)
    }
    // !Printing arguments

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
    list match {
      case Nil => map
      case "--help" :: tail =>
                  getArgsAsMap(map ++ Map(helpKey -> helpKey), tail)
      case "--debug" :: tail =>
                  getArgsAsMap(map ++ Map(debugKey -> debugKey), tail)
      case someKey :: value :: tail if someKey.startsWith("--") =>
                  getArgsAsMap(map ++ Map(someKey -> value), tail)
      case option :: tail =>
                  println("Unknown option "+option); map
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