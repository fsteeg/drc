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

/**
 * Representation of a scanned page.
 * @param words The list of words this page consists of
 * @param id An ID for this page (TODO: update to e.g. URI)
 * @author Fabian Steeg
 */
case class Page(words:List[Word], id: String) {
  
  def toXml = <page> { words.toList.map(_.toXml) } </page>
  
  def save(file:java.io.File): Node = {
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
    
  /* TODO: would need conversion for different page sizes */
  /* TODO: calculate letter width based on number of letters in line */
  /* TODO: get font size from PDF, adjust height and width accordingly */
  /* TODO: treat capital letters differently */
  val widths = Map('l' -> 2, 'i' -> 2, '∫' -> 4, 't' -> 4, 'f' -> 4, 'j' -> 2)
  val defaultWidth = 7
  val boxHeight = 15
    
  def convert(pdfLocation : String) : Page = {
    val words: Buffer[Word] = Buffer()
    val paragraphs : Buffer[Paragraph] = PositionParser.parse(pdfLocation)
    for(p <- paragraphs) {
      for(line <- p.getLines) {
        var pos = line.getStartPointScaled(600, 960)
        for(word <- line.getWords) {
          val wordWidth = width(word)
          words add Word(word, Box(pos.x.toInt, pos.y.toInt - boxHeight, wordWidth, boxHeight))
          /* Update the starting position for the next word: */
          pos = Point(pos.x + wordWidth + defaultWidth, pos.y)
        }
      }
      words add Word("@", Box(0,0,0,0))
    }
    Page(words.toList, new java.io.File(pdfLocation).getName().replace("pdf", "xml"))
  }
    
  def width(word: String) : Int = {
    var result = 0
    for(c <- word.toCharArray) {
      widths get c match {
        case Some(x) => result += x
        case None => result += defaultWidth
      }
    }
    result
  }
}
