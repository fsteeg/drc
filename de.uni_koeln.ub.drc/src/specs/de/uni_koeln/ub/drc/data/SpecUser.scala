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
    it("has a reputation that is adjusted according to upvoting, downvoting, being upvoted and being downvoted") {
      expect(0) { user.reputation }
      expect(true) { val prev = user.reputation; user.hasUpvoted; user.reputation > prev }
      expect(true) { val prev = user.reputation; user.wasUpvoted; user.reputation > prev }
      expect(true) { val prev = user.reputation; user.hasDownvoted; user.reputation < prev }
      expect(true) { val prev = user.reputation; user.wasDownvoted; user.reputation < prev }
    }
    it("can be persisted via XML"){
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
    it("allows loading of a user from XML"){
      expect(user) { User.fromXml(user.toXml) }
    }
    it("allows access to individual saved user instances via user name"){
      val fsteeg = User("fsteeg", "Fabian Steeg", "Cologne, Germany", "")
      val claesn = User("claesn", "Claes Neuefeind", "Cologne, Germany", "")
      val matana = User("matana", "Mihail Atanassov", "Cologne, Germany", "")
      val rols = User("rols", "Jürgen Rolshoven", "Cologne, Germany", "")
      val ocr = User("OCR", "OCR", "Russia", "")
      expect(fsteeg) { User.withId(db, "fsteeg") }
      expect(claesn) { User.withId(db, "claesn") }
      expect(matana) { User.withId(db, "matana") }
      expect(rols) { User.withId(db, "rols") }
      expect(ocr) { User.withId(db, "OCR") }
    }
  }
}