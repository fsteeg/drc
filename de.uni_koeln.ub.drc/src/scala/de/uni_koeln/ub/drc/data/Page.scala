/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/

package de.uni_koeln.ub.drc.data
import scala.xml._
import java.io._
import java.util.zip._
import de.uni_koeln.ub.drc.reader.Point

/**
 * Representation of a scanned page.
 * @param words The list of words this page consists of
 * @param id An ID for this page (TODO: update to e.g. URI)
 * @author Fabian Steeg
 */
case class Page(words:List[Word], id: String) {
  
  var image: Option[ZipEntry] = None
  var zip: Option[ZipFile] = None
  
  def toXml = <page> { words.toList.map(_.toXml) } </page>
  
  def toText = ("" /: words) (_ + " " + _.history.top.form.replace("@", "|")) 
  
  def save(): Node = {
    println("Attempting to save to: " + id)
    val file = new de.schlichtherle.io.File(id)
    val root = toXml
    val formatted = new StringBuilder
    new PrettyPrinter(120, 2).format(root, formatted)
    // XML.saveFull("out.xml", root, "UTF-8", true, null) // FIXME hangs
    val writer = new OutputStreamWriter(new de.schlichtherle.io.FileOutputStream(file), "UTF-8");
    writer.write(formatted.toString)
    writer.close
    root
  }
  
  def save(file:File): Node = {
    val root = toXml
    val formatted = new StringBuilder
    new PrettyPrinter(120, 2).format(root, formatted)
    // XML.saveFull("out.xml", root, "UTF-8", true, null) // FIXME hangs
    val writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
    writer.write(formatted.toString)
    writer.close
    root
  }
  
}

object Page {

  def fromXml(page:Node, id: String): Page = Page( 
    for(word <- (page \ "word").toList) yield Word.fromXml(word), id
  )
  
  def fromPdf(pdf:String): Page = { PdfToPage.convert(pdf) }
  
  def load(file:java.io.File): Page = {
      val page:Node = XML.loadFile(file)
      Page.fromXml(page, file.getName)
  }
  
  def load(stream:java.io.InputStream, id: String): Page = {
      val page:Node = XML.load(stream)
      Page.fromXml(page, id)
  }
  
  def load(id: String, zip: ZipFile, xml: ZipEntry, image: ZipEntry): Page = {
      val page:Node = XML.load(zip.getInputStream(xml))
      val result = Page.fromXml(page, id)
      result.image = Some(image)
      result.zip = Some(zip)
      result
  }

  /**
   * This models what we get from the OCR: the original word forms as recognized by the OCR,
   * together with their coordinates in the scan result (originally a PDF with absolute values).
   */  
  private val map = Map(
      "daniel" -> Box(130, 283, 150, 30),
      "bonifaci" -> Box(280, 285, 180, 30),
      "catechismus" -> Box(70, 330, 80, 20),
      "als" -> Box(110, 390, 30, 20),
      "slaunt" -> Box(78, 498, 45, 15)
  )
  
  /**
   * This models the other part we get from the OCR: the full text, which we need to tokenize and
   * convert into Word objects to be displayed and edited in the UI.
   */
  val mock: Page = 
    Page(
      for( w <- "Daniel Bonifaci Catechismus Als Slaunt".split(" ").toList ) 
        yield Word(w, map(w.toLowerCase)), "mock"
    )
    
}

/** 
 *  Experimental heuristics for creating an XML page representation from a scanned PDF.
 *  Includes computation of the highlighting box based on line start coordinated read from the PDF.
 *  @author Fabian Steeg (fsteeg) 
 */
private object PdfToPage {
    
  import java.net.URL
  import de.uni_koeln.ub.drc.reader._
  import scala.collection.JavaConversions._
  import scala.collection.mutable.Buffer
  import java.io.File
    
  val boxHeight = 15
    
  def convert(pdfLocation : String) : Page = {
    val words: Buffer[Word] = Buffer()
    val paragraphs : Buffer[Paragraph] = PositionParser.parse(pdfLocation)
    val pageHeight = 960
    for(p <- paragraphs) {
      for(line <- p.getLines) {
        var pos = line.getStartPointScaled(600, pageHeight)
        for(word <- line.getWords) {
          val scaled = line.getFontSizeScaled(pageHeight)
          val wordWidth = width(word, scaled)
          words add Word(word, Box(pos.getX.toInt, pos.getY.toInt - scaled, wordWidth, scaled))
          /* Update the starting position for the next word: */
          pos = new Point(pos.getX + wordWidth + scaled, pos.getY)
        }
      }
      words add Word("@", Box(0,0,0,0))
    }
    Page(words.toList, new java.io.File(pdfLocation).getName().replace("pdf", "xml"))
  }
    
  def width(word: String, height: Int) : Int = {
    /* TODO: calculate letter width based on number of letters in line */
    /* TODO: get font spacing and style from PDF, adjust width accordingly */
    var result = 0f
    val narrow = "li∫tfj"
    for(c <- word.toCharArray) {
      /* heuristic: provide width as percentage of height, depending on letter and case: */
      if(narrow.contains(c.toLower)) 
        result += height * (if(c.isLower) .1f else .2f)
      else 
        result += height * (if(c.isLower) .5f else .8f)
    }
    result.round
  }
}
