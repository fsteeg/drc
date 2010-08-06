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
import org.scalatest.BeforeAndAfterAll
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import dispatch._
import dispatch.json._
import dispatch.json.JsHttp._
import scouch.db.Options._
import scouch.db.BulkDocument._
import scouch.db._
import scala.reflect._
import scala.annotation.target._
import sjson.json._ 

/**
 * Initial tests of CouchDB for page persistence.
 * @author Fabian Steeg
 */
@RunWith(classOf[JUnitRunner])
class SpecStorage extends Spec with ShouldMatchers with BeforeAndAfterAll  {

  val http = new Http
  val db = Db(Couch(), "test") // these tests expect CouchDB to be running at 127.0.0.1 on port 5984

  override def beforeAll {
    http(db create)
    http(db as_str) should startWith("""{"db_name":"test","doc_count":0""")
    println("created testing database")
  }
  
  override def afterAll {
    http(db delete)
    (http x db) { (status, _, _) => status } should equal (404)
    println("destroyed testing database")
  }
  
  val page = Page(List(Word("test", Box(1,1,1,1))), "mock")
  
  describe("The DB") {
    it("can be used to store and retrieve pages") {
        val http = new Http
        val id = "page1"
        val doc = Doc(db, id)
        http(doc add page)
        val res = http(db.get[Page](id))
        val retrieved = res._3
        retrieved.getClass should equal(classOf[Page])
        // retrieved.toString should equal(page.toString) // TODO fix issues with aggregated types
      }
    }
}
