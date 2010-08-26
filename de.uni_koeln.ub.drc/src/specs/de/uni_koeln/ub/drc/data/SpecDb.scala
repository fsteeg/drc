/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.data

import scala.xml.Elem
import scala.xml.Node
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import Db._
import java.io.File
/**
 * @see Db
 * @author Fabian Steeg
 */
@RunWith(classOf[JUnitRunner])
class SpecDb extends Spec with ShouldMatchers {
  describe("The Db") {
    it("allows access to all stored pages of a given volume") {
      expect(218 * 2) { Db.ids("PPN345572629_0004").get.size }
    }
    it("allows access to specific pages, both XML and IMG") {
      val coll = "PPN345572629_0004"
      expect(2) { Db.xml(coll, "PPN345572629_0004-0001.xml", "PPN345572629_0004-0002.xml").get.size }
      expect(classOf[Elem]) { Db.xml(coll, "PPN345572629_0004-0001.xml").get(0).getClass }
      expect(classOf[Array[Byte]]) { Db.img(coll, "PPN345572629_0004-0001.jpg").get(0).getClass }
    }
    it("allows to retrieve XML for manipulation and store it back") {
      val now = System.currentTimeMillis
      expect(true) {
        val collection = "PPN345572629_0004"
        val entry = "PPN345572629_0004-0001.xml"
        val rep = "DANiEL"
        val xml = Db.xml(collection, entry).get(0)
        val newXml = xml.toString.replace(rep, rep + now)
        println(newXml)
        Db.put(newXml, collection, entry, DataType.XML)
        val updated = Db.xml(collection, entry).get(0)
        updated.toString.contains(now + "")
      }
    }
    it("stores XML that can be used to instantiate page objects") {
      expect(true){
        val collection = "PPN345572629_0004"
        val pages = Index.loadPagesFromDb(collection)
        pages.forall((p:Page)=>(
            p.getClass==classOf[Page]
            &&p.imageBytes == None)
            &&Index.loadImageFor(pages(0)).isInstanceOf[Array[Byte]])
      }
    }
  }
}