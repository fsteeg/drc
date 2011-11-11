/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
 
package de.uni_koeln.ub.drc.util

import org.junit.runner.RunWith
import scala.xml._
import java.io._
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import Configuration._

/**
 * @see MetsTransformer
 * @author Fabian Steeg (fsteeg)
 */
@RunWith(classOf[JUnitRunner])
class SpecMetsTransformer extends Spec with ShouldMatchers {
  
  describe("The Chapter case class") {
    it("provides ordering first by volume, then by number") {
      expect(List(
          Chapter(4,1,""),
          Chapter(4,2,""),
          Chapter(4,3,""),
          Chapter(5,1,""),
          Chapter(5,2,""),
          Chapter(5,3,"")
      )) {List(
          Chapter(5,3,""),
          Chapter(5,2,""),
          Chapter(5,1,""),
          Chapter(4,3,""),
          Chapter(4,2,""),
          Chapter(4,1,"")
      ).sorted}
    }
  }

  describe("The MetsImporter") {
    
    /* A single file: */
    val file = new File("res/tests/PPN345572629_0004.xml")
    val mets = new MetsTransformer(XML.loadFile(file))
    it("should import METS metadata to ContentDM for single file " + file) {
      mets.transform().length should be > 500
    }
    
    it("should return chapters for specific pages, using order numbers from the metadata") {
      val mode = Count.Label
      expect((1, "Daniel Bonifaci")) { val c = mets.chapter(1, mode); (c.number, c.title) }
      expect((1, "Daniel Bonifaci")) { val c = mets.chapter(8, mode); (c.number, c.title) }
      expect((2, "Gion Antoni Calvenzano")) { val c = mets.chapter(9, mode); (c.number, c.title) }
      expect((2, "Gion Antoni Calvenzano")) { val c = mets.chapter(29, mode); (c.number, c.title) }
      expect((39, "Nachwort")) { val c = mets.chapter(209, mode); (c.number, c.title) }
      expect((39, "Nachwort")) { val c = mets.chapter(218, mode); (c.number, c.title) }
    }
    
    it("should return chapters for specific pages, using physical file numbers") {
      val mode = Count.File
      expect((1, "Daniel Bonifaci")) { val c = mets.chapter(7, mode); (c.number, c.title) }
      expect((1, "Daniel Bonifaci")) { val c = mets.chapter(14, mode); (c.number, c.title) }
      expect((2, "Gion Antoni Calvenzano")) { val c = mets.chapter(15, mode); (c.number, c.title) }
      expect((2, "Gion Antoni Calvenzano")) { val c = mets.chapter(35, mode); (c.number, c.title) }
      expect((39, "Nachwort")) { val c = mets.chapter(215, mode); (c.number, c.title) }
      expect((39, "Nachwort")) { val c = mets.chapter(224, mode); (c.number, c.title) }
    }
    
    it("provides a fallback result for unknown pages") {
      val mode = Count.File
      expect((Integer.MAX_VALUE, "Unknown")) { val c = mets.chapter(Integer.MAX_VALUE, mode); (c.number, c.title) }
      expect((Integer.MAX_VALUE, "Unknown")) { val c = mets.chapter(Integer.MIN_VALUE, mode); (c.number, c.title) }
      expect("Chapitel X: Unknown") { mets.chapter(Integer.MIN_VALUE, mode).toString }
    }
    
    it("provides conversion from file numbers to page labels") {
      expect("1") { mets.label(7) }
      expect("8") { mets.label(14) }
      expect("9") { mets.label(15) }
      expect("29") { mets.label(35) }
      expect("209") { mets.label(215) }
      expect("218") { mets.label(224) }
    }
    
    /* All files: */
    for(dir <- new File(Romafo).list if !dir.startsWith(".") && !dir.startsWith("romafo")) {
      val xmlFile = new File(Romafo+dir, dir+".xml")
      if(xmlFile.exists) {
        it("should import METS metadata to ContentDM for " + xmlFile) {
          val result = new MetsTransformer(XML.loadFile(xmlFile)).transform()
          result.length should be > 500
          if(Write) {
            val out = Cdm+xmlFile.getName.replace(".xml", ".txt")
            val writer = new FileWriter(out);
            writer.write(result); writer.close
          }
        }
      }
    }
    
  }
  
}