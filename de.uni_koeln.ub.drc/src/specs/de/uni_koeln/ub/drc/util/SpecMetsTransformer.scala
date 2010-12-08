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

  describe("The MetsImporter") {
    
    /* A single file: */
    val file = new File(Romafo + "PPN345572629_0004/PPN345572629_0004.xml")
    val transformer = new MetsTransformer(XML.load(new FileReader(file)))
    it("should import METS metadata to ContentDM for single file " + file) {
      transformer.transform().length should be > 500
    }
    
    it("should return chapters for specific pages, using order numbers from the metadata") {
      val mode = Count.Label
      expect("Chapter 1: Daniel Bonifaci") { transformer.chapter(1, mode) }
      expect("Chapter 1: Daniel Bonifaci") { transformer.chapter(8, mode) }
      expect("Chapter 2: Gion Antoni Calvenzano") { transformer.chapter(9, mode) }
      expect("Chapter 2: Gion Antoni Calvenzano") { transformer.chapter(29, mode) }
      expect("Appendix: Nachwort") { transformer.chapter(209, mode) }
      expect("Appendix: Nachwort") { transformer.chapter(218, mode) }
    }
    
    it("should return chapters for specific pages, using physical file numbers") {
      val mode = Count.File
      expect("Chapter 1: Daniel Bonifaci") { transformer.chapter(7, mode) }
      expect("Chapter 1: Daniel Bonifaci") { transformer.chapter(14, mode) }
      expect("Chapter 2: Gion Antoni Calvenzano") { transformer.chapter(15, mode) }
      expect("Chapter 2: Gion Antoni Calvenzano") { transformer.chapter(35, mode) }
      expect("Appendix: Nachwort") { transformer.chapter(215, mode) }
      expect("Appendix: Nachwort") { transformer.chapter(224, mode) }
    }
    
    /* All files: */
    for(dir <- new File(Romafo).list if !dir.startsWith(".") && !dir.startsWith("romafo")) {
      val xmlFile = new File(Romafo+dir, dir+".xml")
      if(xmlFile.exists) {
        val xml = XML.load(new FileReader(xmlFile))
        it("should import METS metadata to ContentDM for " + xmlFile) {
          val result = new MetsTransformer(xml).transform()
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