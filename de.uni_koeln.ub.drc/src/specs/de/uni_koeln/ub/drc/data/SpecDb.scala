/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.data

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
      expect(218 * 2) { Db.get("PPN345572629_0004").size }
    }
    it("allows access to specific pages, both XML and IMG") {
      val res = Db.get("PPN345572629_0004", "PPN345572629_0004-0001.xml", "PPN345572629_0004-0001.jpg")
      expect(2) { res.size }
      expect(classOf[String]) { res(0).getClass }
      expect(classOf[Array[Byte]]) { res(1).getClass }
    }
    it("allows to retrieve XML for manipulation and store it back") {
      val now = System.currentTimeMillis
      expect(true) {
        val collection = "PPN345572629_0004"
        val entry = "PPN345572629_0004-0001.xml"
        val rep = "DANiEL"
        val xml = Db.get(collection, entry)(0)
        val newXml = xml.toString.replace(rep, rep + now)
        println(newXml)
        Db.put(newXml, collection, entry, DataType.XML)
        val updated = Db.get(collection, entry)(0)
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