package net.shchastnyi.medical.tradeexchange

import java.io.{File, PrintWriter}

import scala.io.Source
import scala.sys.process._

/**
 * Class for bulk converting doc files to docx<br/>
 * Uses <i>ofc.exe</i> behind the scenes.<br/>
 * Uses <i>ofc.ini</i> config file which is located at <b>ofcDir/Tools</b>.<br/>
 * To know more about OFC please refer to <a href="http://technet.microsoft.com/en-us/library/cc179019%28v=office.14%29.aspx">http://technet.microsoft.com/en-us/library/cc179019%28v=office.14%29.aspx</a><br/>
 * @param ofcDir A directory where The Office Migration Planning Manager (OMPM) has been installed
 * @param sourceFolder """d:\\tmp\\scala"""
 * @param destinationFolder """d:\\tmp\\scala\\converted"""
 */
class DocToDocx(ofcDir: String, sourceFolder: String, destinationFolder: String) {

  /**
   * Start bulk converting files given the correct installation path to Microsoft OMPM
   * @return Returns the exit code. Standard output and error are sent to the console.
   */
  def convert: Int = {
    val toolsDir = ofcDir + "Tools/"
    val convertingCommand = toolsDir + "ofc.exe " + toolsDir + "ofc.ini"
    convertingCommand.!
  }

  /**
   * fldr=d:\tmp\scala //FoldersToConvert
   */
  def setFoldersToConvert {
    val func = (x: String) => x.replaceAll("(fldr=){1}(.)*", "fldr="+sourceFolder)
    replaceInFile(func)
  }

  /**
   * DestinationPathTemplate=d:\tmp\scala\converted
   */
  def setDestinationPathTemplate {
    val func = (x: String) => x.replaceAll("(DestinationPathTemplate=){1}(.)*", "DestinationPathTemplate="+destinationFolder)
    replaceInFile(func)
  }

  private def replaceInFile(func: (String) => (String)) = {
    val iniString = ofcDir + "Tools/ofc.ini"

    val source = Source.fromFile(iniString)
    val lines = source.mkString.split("\\n").toList.map(func)
    source.close()

    val writer = new PrintWriter(iniString)
    for(line <- lines)
      writer.print(line)
    writer.close()
  }

}
