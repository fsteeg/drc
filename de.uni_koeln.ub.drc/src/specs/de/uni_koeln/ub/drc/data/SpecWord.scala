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
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
/**
 * @see Word
 * @author Fabian Steeg (fsteeg)
 */
@RunWith(classOf[JUnitRunner])
private[drc] class SpecWord extends Spec with ShouldMatchers {

  describe("A Word") {
    val word = Word("test", Box(1, 1, 1, 1))
    it("should contain an original form") { expect("test") { word.original } }
    it("should contain a position") { expect(Box(1, 1, 1, 1)) { word.position } }
    it("should contain a first history entry") {
      expect(Modification("test", "OCR")) { word.history(0) }
    }
    it("can be serialized as XML") {
      expect(word.original) { (word.toXml \ "@original")(0).text.trim }
    }
    it("can be deserialized from XML") {
      expect(1) {
        Word.fromXml(<word original="test">
                       <box width="1" height="1" y="1" x="1"></box>
                       <modification form="test" score="1" author="OCR" date="123456789">
                         <voters>
                           <voter name="me"></voter>
                         </voters>
                       </modification>
                     </word>).history.top.score
      }
    }
    it("provides edit suggestions") {
      val word1 = Word("slaunt", Box(1, 1, 1, 1))
      val word2 = Word("slaunt", Box(1, 1, 1, 1))
      word2.history push Modification("test", "me")
      val suggestions1 = word1.suggestions
      val suggestions2 = word2.suggestions
      println("Suggestions1: " + suggestions1.mkString(", "))
      println("Suggestions2: " + suggestions2.mkString(", "))
      expect(5) { suggestions1.size }
      expect(false) { suggestions2.exists(_.startsWith("s")) } // should use 'test', not 'slaunt'
      expect(31916) { Index.lexicon.size }
    }
    it("provides no suggestions if they are too far off") {
      val word = Word("satanarchäolügenialkohöllischewunschpunsch", Box(1, 1, 1, 1))
      expect(0) { word.suggestions.size }
    }
    it("can be enriched with free tags") {
      val time = System.currentTimeMillis
      word.annotations += Annotation("random", "stuff", "fsteeg", time)
      word.annotations += Annotation("more", "cool stuff", "fsteeg", time)
      expect("stuff") { Word.fromXml(word.toXml).annotations(0).value }
      expect("cool stuff") { Word.fromXml(word.toXml).annotations(1).value }
      expect("fsteeg") { Word.fromXml(word.toXml).annotations(0).user }
      expect(time) { Word.fromXml(word.toXml).annotations(0).date }
    }
  }

}