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
                            <modification form="test" score="1" author="OCR" date="123456789">
                              <voters>
                                <voter name="me"></voter>
                              </voters>
                            </modification>
                          </word>
                        </page>, file.getAbsolutePath).words(0).history.top.score

        }
    }
    
    it("can save a page of words to the DB") {
      Page.mock.saveToDb()
      expect(true) {file.exists}
    }
    
    it("will save all embedded objects") {
      expect(1) {
          page.words(0).history.top.upvote("me")
          page.saveToDb()
          Page.fromXml(page.toXml, page.id).words(0).history.top.score
      }
      expect(1) {
          Page.fromXml(page.toXml, page.id).words(0).history.size
      }
    }
    
  }
  
  describe("The Page companion object") {
  
    it("should provide usable test data") { expect(5) { Page.mock.words.size } }
  
    it("can load a page of words from XML") {
      val words: List[Word] = Page.fromXml(Page.mock.toXml, page.id).words
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
      page.saveToDb()
      val loadedWord = Page.fromXml(page.toXml, page.id).words(0)
      val loadedMod = loadedWord.history.top
      expect(2) { loadedWord.history.size }
      expect(newMod) { loadedMod }
    }
    
    it("should be desializable from an XML string") {
        val loadedFromXml = Page.fromXml(page.toXml, page.id).words(0)
        expect(page.words(0).history.size) { loadedFromXml.history.size }
    }
    
    it("can merge multiple lists of pages into a single list of pages") {
      
      // we assume three pages with identical IDs, edited by three users:
      val o = Modification("test", "OCR")
      val m1 = Modification("c1", "u1"); val p1 = edit(threePages, m1)
      val m2 = Modification("c2", "u2"); val p2 = edit(threePages, m2)
      val m3 = Modification("c3", "u3"); val p3 = edit(threePages, m3)
      
      // assert that each user's modification is present in his three lists:
      for((m,p) <- List((m1,p1), (m2,p2), (m3,p3))) {
        expect(3) { p.size }
        expect(List(m,m,m,m,m,m)) { for(page<-p;word<-page.words) yield word.history.toList(0) }
        expect(List(o,o,o,o,o,o)) { for(page<-p;word<-page.words) yield word.history.toList(1) }
      }
      
      // merge the three lists of each user into three lists containing all modifications:
      val merged = Page.merge(p1, p2, p3)
      
      // the history of each word of the three merged lists should now contain all modifications:
      expect(3) { merged.size }
      for(page <- merged) {
        expect(List(m3,m3)) { for(word<-page.words) yield word.history.toList(0) }
        expect(List(m2,m2)) { for(word<-page.words) yield word.history.toList(1) }
        expect(List(m1,m1)) { for(word<-page.words) yield word.history.toList(2) }
        expect(List(o,o)) { for(word<-page.words) yield word.history.toList(3) }
      }
      
    }
    
    def threePages = List(
        Page(List(Word("test", Box(1,1,1,1)), Word("test", Box(1,1,1,1))), "p1"),
        Page(List(Word("test", Box(1,1,1,1)), Word("test", Box(1,1,1,1))), "p2"),
        Page(List(Word("test", Box(1,1,1,1)), Word("test", Box(1,1,1,1))), "p3")
    )
    
    def edit(pages:List[Page], mod:Modification) = {
      for (page <- pages; word <- page.words) word.history.push(mod); pages
    }
      
    }
  
    it("provides initial import of a scanned PDF") {
          val page : Page = Page.fromPdf("res/rom/PPN345572629_0004/PPN345572629_0004-0001.pdf")
          val file = new java.io.File("res/rom/PPN345572629_0004/PPN345572629_0004-0001.xml")
          val root = page.saveToDb()
          val uri = file.toURI.toURL.toURI
          expect(true) {root.size > 0}
          expect(true) {file.exists}
      }
  
}
