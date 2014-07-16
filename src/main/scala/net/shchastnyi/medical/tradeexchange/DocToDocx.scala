package net.shchastnyi.medical.tradeexchange

import scala.sys.process._

/**
 * Class for bulk converting doc files to docx<br/>
 * Uses <i>ofc.exe</i> behind the scenes.<br/>
 * Uses <i>ofc.ini</i> config file which is located at <b>ofcDir/Tools</b>.<br/>
 * To know more about OFC please refer to <a href="http://technet.microsoft.com/en-us/library/cc179019%28v=office.14%29.aspx">http://technet.microsoft.com/en-us/library/cc179019%28v=office.14%29.aspx</a><br/>
 * @param ofcDir A directory where The Office Migration Planning Manager (OMPM) has been installed
*/
class DocToDocx(ofcDir: String) {

  /**
   * Start bulk converting files given the correct installation path to Microsoft OMPM
   * @return Returns the exit code. Standard output and error are sent to the console.
   */
  def convert: Int = {
    val toolsDir = ofcDir + "Tools/"
    val convertingCommand = toolsDir + "ofc.exe " + toolsDir + "ofc.ini"
    convertingCommand.!
  }

}
