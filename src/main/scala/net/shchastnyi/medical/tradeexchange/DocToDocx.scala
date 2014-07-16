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

  def setFoldersToConvert {
    //fldr=d:\tmp\scala //FoldersToConvert
    val replaceString = "[Run]"
    val replacement = "OLOLO"
    replaceInFile(replaceString, replacement)
  }

  def setDestinationPathTemplate {
    //DestinationPathTemplate=d:\tmp\scala\converted
  }

  def replaceInFile(replaceString:String, replacement:String) = {
    val iniString = ofcDir + "Tools/ofc.ini"

    val source = Source.fromFile(iniString)
    val lines = source
      .mkString.split("\n").toList
      .map( _.replaceAll(replaceString, replacement) )
    source.close()

    val writer = new PrintWriter(iniString)
    for(line <- lines)
      writer.println(line)
    writer.close()
  }

}
