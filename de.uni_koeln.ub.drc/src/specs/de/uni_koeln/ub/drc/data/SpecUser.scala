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
  val user = User("fsteeg", "Fabian Steeg", "Cologne, Germany")
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
    it("can be persisted via XML"){
      expect(<user name="Fabian Steeg" id="fsteeg" region="Cologne, Germany"/>) { user.toXml }
    }
  }
  describe("The User companion") {
    it("allows loading of a user from XML"){
      expect(user) { User.fromXml(user.toXml) }
    }
    it("allows access to individual saved user instances via user name"){
      val fsteeg = User("fsteeg", "Fabian Steeg", "Cologne, Germany")
      val claesn = User("claesn", "Claes Neuefeind", "Cologne, Germany")
      val matana = User("matana", "Mihail Atanassov", "Cologne, Germany")
      val rols = User("rols", "Jürgen Rolshoven", "Cologne, Germany")
      val location = "users"
      User.save(location, fsteeg, claesn, matana, rols)
      expect(fsteeg) { User.withId("fsteeg", location) }
      expect(claesn) { User.withId("claesn", location) }
      expect(matana) { User.withId("matana", location) }
      expect(rols) { User.withId("rols", location) }
    }
  }
}