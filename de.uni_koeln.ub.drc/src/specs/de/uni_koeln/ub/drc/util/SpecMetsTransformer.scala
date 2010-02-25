/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
 
package de.uni_koeln.ub.drc.util

import scala.xml._
import java.io._
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import Configuration._

/**
 * @see MetsTransformer
 * @author Fabian Steeg (fsteeg)
 */
private[drc] class MetsTransformerSpec extends Spec with ShouldMatchers {

  describe("The MetsImporter") {
    
    /* A single file: */
    val file = new File(Romafo + "PPN345572629_0017/PPN345572629_0017.xml")
    it("should import METS metadata to ContentDM for single file " + file) {
      new MetsTransformer(XML.load(new FileReader(file))).transform().length should be > 500
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