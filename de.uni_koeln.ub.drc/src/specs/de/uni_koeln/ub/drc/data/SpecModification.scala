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
 * @see Modification
 * @author Fabian Steeg (fsteeg)
 */
@RunWith(classOf[JUnitRunner])
private[drc] class SpecModification extends Spec with ShouldMatchers {

  describe("A Modification") {
    val before = System.currentTimeMillis
    val mod = Modification("test", "fsteeg")
    val after = System.currentTimeMillis
    it("contains a date") { expect(true) { mod.date >= before && mod.date <= after } }
    it("preserves the original date") { expect(mod.date) { Modification.fromXml(mod.toXml).date } }
    it("contains the edited form") { expect("test") { mod.form } }
    it("contains an author ID") { expect("fsteeg") { mod.author } }
    it("has a score that is adjusted according to votes") {
        expect(0) {mod.score}
        expect(true) { val prev = mod.score; mod.upvote("he"); mod.score > prev }
        expect(true) { val prev = mod.score; mod.downvote("me"); mod.score < prev }
    }
    it("maintains a list of voters") {
        expect(true){ mod.upvote("she"); mod.voters.contains("she") }
        expect(mod.voters){ Modification.fromXml(mod.toXml).voters }
    }
    it("ensures that users that already voted can't vote again") {
        intercept[IllegalArgumentException] { mod.upvote("me"); mod.upvote("me") }
        intercept[IllegalArgumentException] { mod.downvote("me"); mod.downvote("me") }
        intercept[IllegalArgumentException] { mod.upvote("me"); mod.downvote("me") }
    }
    it("can be serialized as XML") { 
        expect(mod) { mod.upvote("sam"); mod.upvote("max"); Modification.fromXml(mod.toXml) }
    }
    it("can be deserialized from XML") {
        expect(1) {
            Modification.fromXml(
                    <modification form="test" score="1" author="OCR" date="123456789">
                      <voters>
                        <voter name="me"></voter>
                      </voters>
                    </modification>
            ).score 
        }
    }
    it("supports XML roundtripping") { expect(mod) { Modification.fromXml(mod.toXml) } }
  }
  
}