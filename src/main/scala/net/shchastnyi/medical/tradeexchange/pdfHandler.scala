package net.shchastnyi.medical.tradeexchange

import java.io._
import org.apache.tika.parser.pdf._
import org.apache.tika.metadata._
import org.apache.tika.parser._
import org.xml.sax._

/**
 * http://www.garysieling.com/blog/extracting-pdf-text-with-scala
 */
object PdfHandler extends ContentHandler {
  def characters(ch : Array[Char], start: Int, length: Int) {
    println(new String(ch))
  }
  def endDocument() {}
  def endElement(uri: String, localName: String, qName: String) {}
  def endPrefixMapping(prefix: String) {}
  def ignorableWhitespace(ch: Array[Char], start: Int, length: Int) {}
  def processingInstruction(target: String, data: String) {}
  def setDocumentLocator(locator: Locator) {}
  def skippedEntity(name: String) {}
  def startDocument() {}
  def startElement(uri: String, localName: String, qName: String, atts: Attributes) {}
  def startPrefixMapping(prefix: String, uri: String) {}
}

class PdfReader {
  def read(filePath: String) = {
    val pdf = new PDFParser()
    val stream = new FileInputStream(filePath)
    val handler = PdfHandler
    val metadata = new Metadata()
    val context = new ParseContext()
    pdf.parse(stream, handler, metadata, context)
    stream.close()
  }
}
