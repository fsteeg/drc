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
class Index(val pages: List[String], val db: XmlDb, val selected: String) {
  
  lazy val pageObjects = pages.toArray.map((id:String) => Page.fromXml(db.getXml(selected, id).get(0)))
  
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
      (option match {
      case SearchOption.all => query("//modification/attribute::form", term)
      case SearchOption.tags => query("//tag/attribute::text", term)
      case SearchOption.comments => query("//comment", term)
    })
    
    private def query(select:String, term:String) = {
      val res = db.query("drc/" + selected, 
          "for $m in %s[ft:query(., '%s')]/ancestor::page return $m".format(select, term.toLowerCase))
      (res\"page").map(Page.fromXml(_)).toArray
    }
      
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
    val all = Value("Text")
    val original = Value("Original")
    val tags = Value("Tags")
    val comments = Value("Comments")
    def toStrings = Array[String]() ++ SearchOption.values map (_.toString)
}

object Import extends Application {
    private val testing = Index.LocalDb
    //val staging = XmlDb("xmldb:exist://hydra2.spinfo.uni-koeln.de:7777/exist/xmlrpc", "db", "drc")
    //val product = XmlDb("xmldb:exist://hydra1.spinfo.uni-koeln.de:8080/exist/xmlrpc", "db", "drc")
    //val anon = XmlDb("xmldb:exist://hydra1.spinfo.uni-koeln.de:8080/exist/xmlrpc", "db", "drc-anonymous")
    
//    Meta.initialImport(db=testing, location="res/rom/PPN345572629")
    User.initialImport(db=testing, folder="users")
    //Index.initialImport(db=testing, location="res/rom/PPN345572629_0004")
    
//    for(volume <- List( // "0033", "0035", "0036", "0037", "0038"
//        "0004", "0008", "0009", "0011", "0012", "0017", "0018", "0024", "0027")) {
//        Index.initialImport(db=testing, location="res/rom/PPN345572629_" + volume)
//    }
    
    
}

object Meta {
  
  def initialImport(collection:String = Index.DefaultCollection, db: XmlDb, location: String): Unit = {
      val folder =  new File(location)
      val files = folder.list
      for(file <- files.toList if file.endsWith("xml") ) {
          val xml = new File(location, file)
          db.put(xml, Format.XML, collection + "/" + folder.getName, xml.getName)
          println("Imported meta xml: " + xml)
      }
  }
  
}

object Index {
    //def collection(s:String) = "drc/" + s
    val DefaultCollection = "drc" 
    val LocalDb = XmlDb("localhost", 7777, "guest", "guest")
    
    lazy val lexicon: Set[String] =
      (Set() ++ scala.io.Source.fromInputStream(
          Index.getClass.getResourceAsStream("words.txt"))("ISO-8859-1").getLines()).map(
              _.replaceAll("\\s[IVX]+", "").trim.toLowerCase)
    
    def loadPagesFromDb(collectionPrefix:String = Index.DefaultCollection, db: XmlDb, collection:String): List[Page] = {
      val ids = db.getIds(collectionPrefix + "/" +collection)
      ids match {
        case Some(list) => for(id <- list; if id.endsWith(".xml"))
          yield Page.fromXml(db.getXml(collectionPrefix + "/" +collection, id).get(0))
        case None => throw new IllegalArgumentException("Invalid collection: " + collectionPrefix + "/" +collection)
      }
    }
    
    def loadImageFor(collection:String=Index.DefaultCollection, db: XmlDb, page:Page): Array[Byte] = {
      val file = page.id.split("/").last // TODO centralize, use extractor?
      db.getBin(collection + "/" +file.split("-")(0), file.replace(".xml", ".png")).get(0)
    }
    
    /** 
     * Import page PDF files to XML.
     * @param location The directory containing PDF files to be imported into the page XML format 
     */
    def initialImport(collection:String=Index.DefaultCollection, db: XmlDb, location: String): Unit = {
        val folder =  new File(location)
        val files = folder.list
        for(file <- files.toList if file.endsWith("pdf") ) {
            val xml = new File(location, file.replace("pdf", "xml").replace(" ", ""))
            val img = new File(xml.getParent, xml.getName.replace(".xml", ".png"))
            // TODO use separate test data (overwriting here)
            val page = Page.fromPdf(new File(location, file).getAbsolutePath)
            XML.save(xml.getAbsolutePath, page.toXml, "UTF-8", false)
            db.put(xml, Format.XML, collection + "/" + folder.getName, xml.getName)
            db.put(img, Format.BIN, collection + "/" + folder.getName, img.getName)
            println("Imported xml: " + xml)
            println("Imported img: " + img)
        }
    }
    
    def apply(pages: List[String], db: XmlDb, collection: String) = new Index(pages, db, collection)
}