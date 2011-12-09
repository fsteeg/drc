/**
 * ************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 * ***********************************************************************************************
 */
package de.uni_koeln.ub.drc.data

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import de.uni_koeln.ub.drc.util.PlainTextCopy
/**
 * @see Index
 * @author Fabian Steeg (fsteeg)
 */
@RunWith(classOf[JUnitRunner])
class SpecIndex extends Spec with ShouldMatchers {

  val page = Page(Word("test", Box(0, 0, 0, 0)) :: Nil, "mock")
  page.comments += Comment("me", "comment text", 0)
  page.tags += Tag("testtag", "me")
  val pages = Page.mock :: page :: Nil
  val db = Index.LocalDb
  val root = "test" + "/" + Index.DefaultCollection
  val coll = "testing"
  for (p <- pages) {
    db.putXml(p.toXml, root + "/" + coll, p.id)
    PlainTextCopy.saveToDb(p, root + PlainTextCopy.suffix + "/", coll, db)
  }
  val ids = pages map (_.id)

  describe("The Index") {
    val index = Index(ids, db, coll, root)
    it("allows full text search for a list of pages") {
      expect(1) { index.search("catechismus").length }
      expect(1) { index.search("Test").length }
      expect(0) { index.search("catechismus".reverse).length }
    }
    it("is case insensitive") {
      expect(index.search("test").toList) { index.search("Test").toList }
    }
    it("supports searching in different fields") {
      expect(1) { index.search("testtag", SearchOption.tags).length }
      expect(1) { index.search("comment", SearchOption.comments).length }
    }
  }

  describe("The Index companion object") {
    it("provides a factory method") {
      expect(Index(ids, db, coll)) { new Index(ids, db, coll) }
    }
  }

}