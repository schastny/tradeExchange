package net.shchastnyi.medical.tradeexchange

import java.io.File

/**
 * Given the list of pdf/docx files, construct an html text with its titles and download links
 */
object FileLister {

  def apply(pathToFiles: String): String = {
    val docxFiles = (new File(pathToFiles)).listFiles().filter(_.getName.endsWith(".docx"))
    ""
  }

}
