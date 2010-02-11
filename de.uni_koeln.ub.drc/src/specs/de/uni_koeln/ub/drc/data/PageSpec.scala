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

/**
 * @see Page
 * @author Fabian Steeg
 */
class PageSpec extends Spec with ShouldMatchers {

  val file: java.io.File = java.io.File.createTempFile("testing", "scala")
  file.deleteOnExit
  
  describe("A Page") {
    
    val page = Page(List(Word("test", Box(1,1,1,1))))
      
    it("should contain words") { 
        expect(1) { page.words.size } 
    }
    
    it("can be serialized to XML") {
        expect(1) { (page.toXml \ "word").size }
    }
  }
  
  describe("The Page companion object") {
  
    it("should provide usable test data") { expect(5) { Page.mock.words.size } }
  
    it("can save a page of words to disk") {
      Page.save(Page.mock, file)
      expect(true) {file.exists}
    }
    
    it("can load a page of words from disk") {
      val words: List[Word] = Page.load(file).words
      expect(Page.mock.words.size) { words.size }
      expect(Page.mock.words.toList) { words.toList }
    }
    
    it("should serialized and deserialize added modifications") {
      val page = Page.mock
      val word = page.words(0)
      val newMod = Modification(word.original.reverse, "tests")
      word.history push newMod
      expect(true) { word.history.contains(newMod) }
      expect(2) { word.history.size }
      Page.save(page, file)
      val loadedWord = Page.load(file).words(0)
      val loadedMod = loadedWord.history.top
      expect(2) { loadedWord.history.size }
      expect(newMod) { loadedMod }
    }
    
  }
  
}
