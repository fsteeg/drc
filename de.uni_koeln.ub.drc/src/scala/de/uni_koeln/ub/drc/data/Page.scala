/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/

package de.uni_koeln.ub.drc.data
import scala.xml._

/**
 * Representation of a scanned page.
 * @param words The list of words this page consists of
 * @author Fabian Steeg
 */
case class Page(words:List[Word]) {
  
  def toXml = <page> { words.toList.map(_.toXml) } </page>
  
  def save(file:java.io.File): Node = {
    val root = toXml
    val formatted = new StringBuilder
    new PrettyPrinter(120, 2).format(root, formatted)
    // XML.saveFull("out.xml", page, "UTF-8", true, null) // FIXME hangs
    val writer = new java.io.FileWriter(file)
    writer.write(formatted.toString)
    writer.close
    root
  }
  
}

object Page {

  def fromXml(page:Node): Page = Page( 
    for(word <- (page \ "word").toList) yield Word.fromXml(word) 
  )
  
  def load(file:java.io.File): Page = {
      val page:Node = XML.loadFile(file)
      Page.fromXml(page)
  }
  
  def load(stream:java.io.InputStream): Page = {
      val page:Node = XML.load(stream)
      Page.fromXml(page)
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
        yield Word(w, map(w.toLowerCase))
    )
    
}
