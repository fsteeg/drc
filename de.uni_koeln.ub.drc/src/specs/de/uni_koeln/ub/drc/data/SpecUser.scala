/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.data

import com.quui.sinist.XmlDb
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
/**
 * @see User
 * @author Fabian Steeg
 */
@RunWith(classOf[JUnitRunner])
class SpecUser extends Spec with ShouldMatchers {
  val user = User("fsteeg", "Fabian Steeg", "Cologne, Germany", "")
  val db = Index.LocalDb
  describe("A User") {
    it("has a unique user name") {
      expect("fsteeg") { user.id }
    }
    it("has a full user name") {
      expect("Fabian Steeg") { user.name }
    }
    it("has an associated region") {
      expect("Cologne, Germany") { user.region }
    }
    it("can be associated with a specific DB location") {
      val default = XmlDb("xmldb:exist://localhost:8080/exist/xmlrpc", "/db/", "drc/")
      expect(default) { user.db }
      expect(default) { User.fromXml(user.toXml).db }
      val customDb = XmlDb("xmldb:exist://hydra2.spinfo.uni-koeln.de:8080/exist/xmlrpc", "/db/", "drc/")
      val customUser = User("fsteeg", "Fabian Steeg", "Cologne, Germany", "", customDb)
      expect(customDb) { customUser.db }
      expect(customDb) { User.fromXml(customUser.toXml).db }
    }
    it("has a reputation that is adjusted according to upvoting, downvoting, being upvoted and being downvoted") {
      expect(0) { user.reputation }
      expect(true) { val prev = user.reputation; user.hasUpvoted; user.reputation > prev }
      expect(true) { val prev = user.reputation; user.wasUpvoted; user.reputation > prev }
      expect(true) { val prev = user.reputation; user.hasDownvoted; user.reputation < prev }
      expect(true) { val prev = user.reputation; user.wasDownvoted; user.reputation < prev }
    }
    it("can store the latest position worked on") {
      user.latestPage = "someid"
      expect("someid") { User.fromXml(user.toXml).latestPage }
      user.latestWord = 5
      expect(5) { User.fromXml(user.toXml).latestWord }
    }
    it("can be persisted via XML") {
      expect(user) { User.fromXml(user.toXml) }
    }
    it("can be changed and persisted") {
      expect(true) {
        user.hasUpvoted; user.save(db); User.withId(db, user.id).reputation > 0
      }
    }
  }
  describe("The User companion") {
    it("allows import of users into the DB") {
      User.initialImport(db, "users")
    }
    it("allows loading of a user from XML") {
      expect(user) { User.fromXml(user.toXml) }
    }
    it("allows access to individual saved user instances via user name") {
      val fsteeg = User("fsteeg", "Fabian Steeg", "Cologne, Germany", "drc", db)
      val claesn = User("claesn", "Claes Neuefeind", "Cologne, Germany", "drc", db)
      val matana = User("matana", "Mihail Atanassov", "Cologne, Germany", "drc", db)
      val rols = User("rols", "Jürgen Rolshoven", "Cologne, Germany", "drc", db)
      val ocr = User("OCR", "OCR", "Russia", "drc", db)
      expect(fsteeg) { User.withId(db, "fsteeg") }
      expect(claesn) { User.withId(db, "claesn") }
      expect(matana) { User.withId(db, "matana") }
      expect(rols) { User.withId(db, "rols") }
      expect(ocr) { User.withId(db, "OCR") }
    }
  }
}