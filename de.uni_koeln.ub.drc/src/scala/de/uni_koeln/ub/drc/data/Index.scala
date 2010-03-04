/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.data
import java.io.File

/**
 * Simple experimental index for initial page selection.
 * @param pages The pages to index
 * @author Fabian Steeg (fsteeg)
 *
 */
class Index(val pages: List[Page]) {
    
    /** 
     * @param term The term to search for
     * @return A list of pages where any word's history contains the term
     */
    def search(term: String): Array[Page] =  
        for { page <- pages.toArray 
            if page.words.exists(_.history.exists(_.form.toLowerCase contains term.toLowerCase))
        } yield page
    
    override def toString = "Index with " + pages.length + " pages"
    override def hashCode = pages.hashCode
    override def equals(other: Any) = other match {
        case that: Index => this.pages == that.pages
        case _ => false
    }
    
}

object Index {
    
    /** 
     * @param location The directory containing the page XML files to load 
     */
    def loadPagesFromFolder(location: String): List[Page] = {
        val files = new File(location).list
        for(file <- files.toList if file.endsWith("xml") && file.contains("-"))
            yield Page.load(new File(location, file))
    }
    
    /** 
     * @param location The directory containing PDF files to be imported into the page XML format 
     */
    def initialImport(location: String): Unit = {
        val files = new File(location).list
        for(file <- files.toList if file.endsWith("pdf") ) {
             val xml = new File(location, file.replace("pdf", "xml"))
             if(!(xml.exists)) { // TODO use separate test data to allow overwriting
                val page = Page.fromPdf(new File(location, file).getAbsolutePath)
                page.save(xml)
            }
        }
    }
    
    def apply(pages: List[Page]) = new Index(pages)
}