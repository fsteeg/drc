/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.data

import scala.xml.XML
import java.io.File
import java.util.zip._
import scala.collection.mutable.ListBuffer
import com.quui.sinist.XmlDb
import com.quui.sinist.XmlDb.Format
/**
 * Simple experimental index for initial page selection.
 * @param pages The pages to index
 * @author Fabian Steeg (fsteeg)
 *
 */
class Index(val pages: List[Page]) {
    /**
     * Search for pages containing a given term.
     * @param term The term to search for
     * @return A list of pages where any word's history contains the term
     */
    def search(term: String): Array[Page] = search(term, SearchOption.all)
    
    /**
     * Search for pages containing a given term.
     * @param term The term to search for
     * @param option The search option, a SearchOption.Value
     * @return A list of pages where any word contains the term according to the specified option
     */
    def search(term: String, option: SearchOption.Value): Array[Page] =
      for { page <- pages.toArray if page.words.exists( 
          option match {
            case SearchOption.all => _.history.exists(_.form.toLowerCase contains term.toLowerCase)
            case SearchOption.latest => _.history.top.form.toLowerCase contains term.toLowerCase
            case SearchOption.original => _.history.toList.last.form.toLowerCase contains term.toLowerCase
          })
      } yield page
    
    override def toString = "Index with " + pages.length + " pages"
    override def hashCode = pages.hashCode
    override def equals(other: Any) = other match {
        case that: Index => this.pages == that.pages
        case _ => false
    }
        
}

object SearchOption extends Enumeration {
    type SearchOption = Value
    val latest = Value("Latest")
    val all = Value("All")
    val original = Value("Original")
    def toStrings = Array[String]() ++ SearchOption.values map (_.toString)
}

object Import extends Application {
    Index.initialImport("res/rom/PPN345572629_0004")
    User.initialImport("users");
}

object Index {
    val Db = XmlDb("xmldb:exist://localhost:8888/exist/xmlrpc", "/db/", "drc/")
    lazy val lexicon: Set[String] =
      (Set() ++ scala.io.Source.fromInputStream(
          Index.getClass.getResourceAsStream("words.txt"))("ISO-8859-1").getLines()).map(
              _.replaceAll("\\s[IVX]+", "").trim.toLowerCase)
    
    def loadPagesFromDb(collection:String): List[Page] = {
      val ids = Db.getIds(collection)
      ids match {
        case Some(list) => for(id <- list; if id.endsWith(".xml"))
          yield Page.fromXml(Db.getXml(collection, id).get(0), id)
        case None => throw new IllegalArgumentException("Invalid collection: " + collection)
      }
    }
    
    def loadImageFor(page:Page): Array[Byte] = {
      val file = page.id.split("/").last // TODO centralize, use extractor?
      Db.getBin(file.split("-")(0), file.replace(".xml", ".jpg")).get(0)
    }
    
    /** 
     * Import page PDF files to XML.
     * @param location The directory containing PDF files to be imported into the page XML format 
     */
    def initialImport(location: String): Unit = {
        val files = new File(location).list
        for(file <- files.toList if file.endsWith("pdf") ) {
            val xml = new File(location, file.replace("pdf", "xml").replace(" ", ""))
            val img = new File(xml.getParent, xml.getName.replace(".xml", ".jpg"))
            // TODO use separate test data (overwriting here)
            val page = Page.fromPdf(new File(location, file).getAbsolutePath)
            XML.save(xml.getAbsolutePath, page.toXml, "UTF-8", false)
            Db.put(xml, Format.XML)
            Db.put(img, Format.BIN)
            println("Imported xml: " + xml)
            println("Imported img: " + img)
        }
    }
    
    def apply(pages: List[Page]) = new Index(pages)
}