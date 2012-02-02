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
      expect((1, "Daniel Bonifaci")) { val c = mets.chapters(1, mode)(0); (c.number, c.title) }
      expect((1, "Daniel Bonifaci")) { val c = mets.chapters(8, mode)(0); (c.number, c.title) }
      expect((2, "Gion Antoni Calvenzano")) { val c = mets.chapters(9, mode)(0); (c.number, c.title) }
      expect((2, "Gion Antoni Calvenzano")) { val c = mets.chapters(29, mode)(0); (c.number, c.title) }
      expect((39, "Nachwort")) { val c = mets.chapters(209, mode)(0); (c.number, c.title) }
      expect((39, "Nachwort")) { val c = mets.chapters(218, mode)(0); (c.number, c.title) }
    }
    
    it("should return chapters for specific pages, using physical file numbers") {
      val mode = Count.File
      expect((1, "Daniel Bonifaci")) { val c = mets.chapters(7, mode)(0); (c.number, c.title) }
      expect((1, "Daniel Bonifaci")) { val c = mets.chapters(14, mode)(0); (c.number, c.title) }
      expect((2, "Gion Antoni Calvenzano")) { val c = mets.chapters(15, mode)(0); (c.number, c.title) }
      expect((2, "Gion Antoni Calvenzano")) { val c = mets.chapters(35, mode)(0); (c.number, c.title) }
      expect((39, "Nachwort")) { val c = mets.chapters(215, mode)(0); (c.number, c.title) }
      expect((39, "Nachwort")) { val c = mets.chapters(224, mode)(0); (c.number, c.title) }
    }

    it("should return multiple chapters for pages on chapter borders - using page label numbers") {
      checkChapterBorder(Count.Label, 29, 30)
    }

    it("should return multiple chapters for pages on chapter borders - using page file numbers") {
      checkChapterBorder(Count.File, 35, 36)
    }

    def checkChapterBorder(mode: Count.Value, endOfChapter2: Int, startOfChapter3: Int) = {
      expect((2, "Gion Antoni Calvenzano")) { val c = mets.chapters(endOfChapter2, mode)(0); (c.number, c.title) }
      // supply optional index: which result to return (default is 0, latest hit)
      expect((2, "Gion Antoni Calvenzano")) { val c = mets.chapters(startOfChapter3, mode)(1); (c.number, c.title) }
      expect((3, "Adam Nauli")) { val c = mets.chapters(startOfChapter3, mode)(0); (c.number, c.title) }
    }

    it("provides a fallback result for unknown pages - using page label numbers") {
      checkFallback(Count.Label)
    }

    it("provides a fallback result for unknown pages - using page file numbers") {
      checkFallback(Count.File)
    }

    def checkFallback(mode: Count.Value) = {
      expect((Integer.MAX_VALUE, "Unknown")) { val c = mets.chapters(Integer.MAX_VALUE, mode)(0); (c.number, c.title) }
      expect((Integer.MAX_VALUE, "Unknown")) { val c = mets.chapters(Integer.MIN_VALUE, mode)(0); (c.number, c.title) }
      expect("Chapitel X: Unknown") { mets.chapters(Integer.MIN_VALUE, mode)(0).toString }
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