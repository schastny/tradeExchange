package net.shchastnyi.medical.tradeexchange

import java.io.{File, FileOutputStream}
import javax.xml.bind.JAXBElement

import org.docx4j.Docx4J
import org.docx4j.convert.out.FOSettings
import org.docx4j.fonts.{IdentityPlusMapper, PhysicalFonts}
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.docx4j.wml.{ContentAccessor, R}

import scala.collection.JavaConverters._

/**
 *
 * @param saveFO Save the intermediate XSL FO. Don't do this in production!
 */
class DocxToPdf(saveFO: Boolean = false) {

  private def getAllElementFromObject(obj: AnyRef, toSearch: Class[_]): List[AnyRef] =
    getAllElementFromObjectInternal(obj, toSearch, List())

  private def getAllElementFromObjectInternal(obj: AnyRef, toSearch: Class[_], result: List[AnyRef]): List[AnyRef] = {
    val objValue =
      if (obj.isInstanceOf[JAXBElement[_]])
        obj.asInstanceOf[JAXBElement[_]].getValue.asInstanceOf[AnyRef]
      else
        obj

    objValue match {
      case x if (x.getClass == toSearch) => objValue :: result
      case x: ContentAccessor => {
        val childrenLists =
          for (child <- (objValue.asInstanceOf[ContentAccessor]).getContent.asScala)
            yield getAllElementFromObject(child, toSearch)
        childrenLists.flatten.toList ++ result
      }
      case _ => result
    }
  }

  /**
   * Removes all hidden text from MS Word docx document (content that is marked with ''vanish'' tag).<br/>
   * Read more about vanish tag for docx <a href="http://msdn.microsoft.com/en-us/library/office/cc547047%28v=office.15%29.aspx">here</a><br/>
   * This tag is present when one translates docx documents with google translate service.
   * @param template WordprocessingMLPackage instance
   * @param clazz Specific
   */
  private def removeVanished(template: WordprocessingMLPackage, clazz: Class[_]) {
    for (r <- getAllElementFromObject(template.getMainDocumentPart, clazz)) {
      val rElement = r.asInstanceOf[org.docx4j.wml.R]
      val rpr = rElement.getRPr
      val condition1 = rpr != null && rpr.getVanish != null && rpr.getVanish.isVal
      val condition2 = rpr != null && rpr.getRStyle != null && (rpr.getRStyle.getVal == "google-src-text1")
      if (condition1 || condition2)
        rElement.getContent.clear
    }
  }

  private def removeStyle(template: WordprocessingMLPackage, styleName: String) {
    for (r <- getAllElementFromObject(template.getMainDocumentPart, classOf[R])) {
      val rElement = r.asInstanceOf[R]
      val rpr = rElement.getRPr
      val condition: Boolean = rpr != null && rpr.getRStyle != null && (rpr.getRStyle.getVal == styleName)
      if (condition)
        rpr.getRStyle.setVal("")
    }
  }

  def convert(inputfilepath: String) {
    // Set regex if you want to restrict to some defined subset of fonts
    val regex = ".*(calibri|camb|cour|arial|times|comic|georgia|impact|LSANS|pala|tahoma|trebuc|verdana|symbol|webdings|wingding).*"
    PhysicalFonts.setRegex(regex)

    // Document loading (required)
    println("Loading file from " + inputfilepath)
    val wordMLPackage = WordprocessingMLPackage.load(new File(inputfilepath))

    // Removing unneeded parts
    removeVanished(wordMLPackage, classOf[org.docx4j.wml.R])
    removeStyle(wordMLPackage, "notranslate")

    // Set up font mapper (optional)
    val fontMapper = new IdentityPlusMapper
    fontMapper.getFontMappings
      .put("Times New Roman", PhysicalFonts.getPhysicalFonts.get("Arial Unicode MS"))
    wordMLPackage.setFontMapper(fontMapper)

    // FO exporter setup (required)
    val foSettings = Docx4J.createFOSettings
    if (saveFO) {
      foSettings.setFoDumpFile(new File(inputfilepath + ".fo"))
    }
    foSettings.setWmlPackage(wordMLPackage)

    // Document format:
    // The default implementation of the FORenderer that uses Apache Fop will output a PDF document if nothing is passed via
    // foSettings.setApacheFopMime(apacheFopMime);
    // apacheFopMime can be any of the output formats defined in
    // org.apache.fop.apps.MimeConstants eg org.apache.fop.apps.MimeConstants.MIME_FOP_IF or
    // FOSettings.INTERNAL_FO_MIME if you want the fo document as the result.
    foSettings.setApacheFopMime(FOSettings.MIME_PDF)

    // exporter writes to an OutputStream.
    val outputfilepath = inputfilepath + ".pdf"
    val os = new FileOutputStream(outputfilepath)

    // Specify whether PDF export uses XSLT or not to create the FO (XSLT takes longer, but is more complete).
    Docx4J.toFO(foSettings, os, Docx4J.FLAG_EXPORT_PREFER_XSL)
    println("Saved: " + outputfilepath)
  }

}

