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

    val db = Index.LocalDb
    val plain = "PPN345572629_0004"
    val collection = Index.DefaultCollection + "/" + plain
    val entry = "PPN345572629_0004-0007.xml"

    it("allows access to specific pages, both XML and IMG") {
      expect(2) { db.getXml(collection, "PPN345572629_0004-0007.xml", "PPN345572629_0004-0008.xml").get.size }
      expect(classOf[Elem]) { db.getXml(collection, "PPN345572629_0004-0007.xml").get(0).getClass }
      expect(classOf[Array[Byte]]) { db.getBin(collection, "PPN345572629_0004-0007.png").get(0).getClass }
    }

    it("allows to retrieve XML for manipulation and store it back") {
      val now = System.currentTimeMillis
      expect(true) {
        val rep = "DANiEL"
        val xml = db.getXml(collection, entry).get(0)
        val newXml = xml.toString.replace(rep, rep + now)
        println(newXml)
        db.putXml(XML.loadString(newXml), collection, entry)
        val updated = db.getXml(collection, entry).get(0)
        updated.toString.contains(now + "")
      }
    }

    it("allows to manipulate entries as page objects and store them back") {
      val now = System.currentTimeMillis
      val xml = db.getXml(collection, entry).get(0)
      val page = Page.fromXml(xml)
      val mod = page.words(0).history.top
      val oldScore = page.words(0).history.top.score
      mod.downvote("tests"+System.currentTimeMillis)
      expect(true) { mod.score < oldScore }
      page.saveToDb(db=db)
      expect(true) { 
        val p = Page.fromXml(db.getXml(collection, entry).get(0)); 
        p.words(0).history.top.score < oldScore }
    }

    it("stores XML that can be used to instantiate page objects") {
      expect(true) {
        val pages = Index.loadPagesFromDb(db=db, collection=plain)
        pages.forall((p: Page) => (p.getClass == classOf[Page]
          && p.imageBytes == None)
          && Index.loadImageFor(db=db, page=pages(0)).isInstanceOf[Array[Byte]])
      }
    }

    it("allows access to all stored pages IDs") {
      expect(218 * 2) { db.getIds(collection).get.size }
    }

    it("returns all entries if no ids are given") {
      expect(218) { db.getXml(collection).get.size }
      expect(218) { db.getBin(collection).get.size }
    }

  }
}