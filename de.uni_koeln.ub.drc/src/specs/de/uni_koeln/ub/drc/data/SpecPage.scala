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
 * @see Page
 * @author Fabian Steeg
 */
@RunWith(classOf[JUnitRunner])
class SpecPage extends Spec with ShouldMatchers {

  val file: java.io.File = java.io.File.createTempFile("testing", "scala")
  file.deleteOnExit
  val page = Page(List(Word("test", Box(1,1,1,1))), "mock")
  
  describe("A Page") {
    
    it("should contain words") { 
        expect(1) { page.words.size } 
    }
    
    it("can be serialized to XML") {
        expect(1) { (page.toXml \ "word").size }
    }
    
    it("can be deserialized from XML") {
        expect(1) {
            Page.fromXml(<page>
                          <word original="test">
                            <box width="1" height="1" y="1" x="1"></box>
                            <modification form="test" score="1" author="OCR">
                              <voters>
                                <voter name="me"></voter>
                              </voters>
                            </modification>
                          </word>
                        </page>, file.getAbsolutePath).words(0).history.top.score

        }
    }
    
    it("can save a page of words to disk") {
      Page.mock.save(file)
      expect(true) {file.exists}
    }
    
    it("will save all embedded objects") {
      val temp = java.io.File.createTempFile("testing", "scala")
      println(temp)
      expect(1) {
          page.words(0).history.top.upvote("me"); page.save(temp)
          Page.load(temp).words(0).history.top.score
      }
      expect(1) {
          Page.load(temp).words(0).history.size
      }
    }
    
  }
  
  describe("The Page companion object") {
  
    it("should provide usable test data") { expect(5) { Page.mock.words.size } }
  
    it("can load a page of words from disk") {
      val words: List[Word] = Page.load(file).words
      expect(Page.mock.words.size) { words.size }
      expect(Page.mock.words.toList) { words.toList }
    }
    
    it("provides roundtrip serialization") {
      expect(page) { Page.fromXml(page.toXml, page.id) }
    }
    
    it("should serialize and deserialize added modifications") {
      val page = Page.mock
      val word = page.words(0)
      val newMod = Modification(word.original.reverse, "tests")
      word.history push newMod
      expect(true) { word.history.contains(newMod) }
      expect(2) { word.history.size }
      page.save(file)
      val loadedWord = Page.load(file).words(0)
      val loadedMod = loadedWord.history.top
      expect(2) { loadedWord.history.size }
      expect(newMod) { loadedMod }
    }
    
    it("should be desializable both from a file and an input stream") {
        val loadedFromFile = Page.load(file).words(0)
        expect(2) { loadedFromFile.history.size }
        val loadedFromStream = Page.load(file.toURI.toURL.openStream, file.getName()).words(0)
        expect(2) { loadedFromFile.history.size }
    }
    
    it("provides initial import of a scanned PDF") {
          val page : Page = Page.fromPdf("res/rom/PPN345572629_0004/PPN345572629_0004-0001.pdf")
          val file = new java.io.File("res/rom/PPN345572629_0004/PPN345572629_0004-0001.xml")
          val root = page.save(file)
          val uri = file.toURI.toURL.toURI
          expect(true) {root.size > 0}
          expect(true) {file.exists}
      }
    
  }
  
}
