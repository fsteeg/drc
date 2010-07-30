/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.data

import org.scalatest._
import org.scalatest.matchers._
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
/**
 * @see Index
 * @author Fabian Steeg (fsteeg)
 */
@RunWith(classOf[JUnitRunner])
class SpecIndex extends Spec with ShouldMatchers {
    
    val pages = Page.mock :: Page.mock :: Page(Word("test", Box(0,0,0,0)) :: Nil, "mock") :: Nil
    
    describe("The Index") {
        val index = Index(pages)
        it("allows full text search for a list of pages") {
            expect(2) {index.search("catechismus").length}
            expect(1) {index.search("Test").length}
            expect(0) {index.search("catechismus".reverse).length}
        }
        it("is case insensitive") {
            expect( index.search("test").toList ) { index.search("Test").toList }
        }
    }
    
    describe("The Index companion object") {
        it("provides a factory method") {
            expect(Index(pages)) { new Index(pages) }
        }
    }
    
}