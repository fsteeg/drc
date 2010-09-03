/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.data

import scala.xml._
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import com.quui.sinist.XmlDb
import java.io.File
/**
 * Test DRC database access and content.
 * @author Fabian Steeg
 */
@RunWith(classOf[JUnitRunner])
class SpecDrcDb extends Spec with ShouldMatchers {
  
  describe("The Db") {
    
    val db = Index.Db
    
    it("allows access to all stored pages IDs") {
      expect(218 * 2) { db.getIds("PPN345572629_0004").get.size }
    }
    
    it("returns all entries if no ids are given") {
      expect(218) { db.getXml("PPN345572629_0004").get.size }
      expect(218) { db.getBin("PPN345572629_0004").get.size }
    }
    
    it("allows access to specific pages, both XML and IMG") {
      val coll = "PPN345572629_0004"
      expect(2) { db.getXml(coll, "PPN345572629_0004-0001.xml", "PPN345572629_0004-0002.xml").get.size }
      expect(classOf[Elem]) { db.getXml(coll, "PPN345572629_0004-0001.xml").get(0).getClass }
      expect(classOf[Array[Byte]]) { db.getBin(coll, "PPN345572629_0004-0001.jpg").get(0).getClass }
    }
    
    it("allows to retrieve XML for manipulation and store it back") {
      val now = System.currentTimeMillis
      expect(true) {
        val collection = "PPN345572629_0004"
        val entry = "PPN345572629_0004-0001.xml"
        val rep = "DANiEL"
        val xml = db.getXml(collection, entry).get(0)
        val newXml = xml.toString.replace(rep, rep + now)
        println(newXml)
        db.putXml(XML.loadString(newXml), collection, entry)
        val updated = db.getXml(collection, entry).get(0)
        updated.toString.contains(now + "")
      }
    }
    
    it("stores XML that can be used to instantiate page objects") {
      expect(true) {
        val collection = "PPN345572629_0004"
        val pages = Index.loadPagesFromDb(collection)
        pages.forall((p: Page) => (p.getClass == classOf[Page]
          && p.imageBytes == None)
          && Index.loadImageFor(pages(0)).isInstanceOf[Array[Byte]])
      }
    }
    
  }
}