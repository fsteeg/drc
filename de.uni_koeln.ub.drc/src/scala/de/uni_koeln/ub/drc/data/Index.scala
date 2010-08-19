/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.data
import java.io.File
import java.util.zip._
import scala.collection.mutable.ListBuffer
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
    for {
      page <- pages.toArray if page.words.exists(
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
}

object Index {
  import com.novocode.squery.session._
  import com.novocode.squery.session.Database.threadLocalSession
  val mem = "jdbc:h2:mem:test1"
  val ser = "jdbc:h2:tcp://localhost/~/test"
  val db = Database.forURL(ser, driver = "org.h2.Driver")
  import com.novocode.squery.combinator.basic.BasicDriver.Implicit._
  import Tables._
  def initDb =
    db withSession {
      (Pages.ddl ++ Words.ddl ++ Modifications.ddl ++ Users.ddl ++ Votes.ddl) create
    }
  def store(pages: List[Page]): Unit = {
    db withSession {
      for (
        page <- pages;
        insPage = Pages insert (page.id, new javax.sql.rowset.serial.SerialBlob("test".getBytes));
        word <- page.words;
        wordId = page.id+"-"+word.hashCode;
        box = word.position;
        insWord = Words insert (wordId, page.id, word.original, box.x, box.y, box.width, box.height);
        mod <- word.history;
        insMod = Modifications insert (wordId+"-"+mod.hashCode, wordId, mod.author, mod.form, mod.score)
      ) println("Hm")
    }
  }

  def retrieve: List[Page] = {
    val res = scala.collection.mutable.ListBuffer[Page]()
    val q = for (page <- Pages) yield page
    db withSession {
      println(q.list())
      for (t <- q) res += Page.fromDb(t, db)
    }
    res.toList
  }

  lazy val lexicon: Set[String] =
    (Set() ++ scala.io.Source.fromInputStream(
      Index.getClass.getResourceAsStream("words.txt"))("ISO-8859-1").getLines()).map(
      _.replaceAll("\\s[IVX]+", "").trim.toLowerCase)

  /** 
   * Load serialized XML pages from a directory.
   * @param location The directory containing the page XML files to load 
   */
  def loadPagesFromFolder(location: String): List[Page] = {
    val files = new File(location).list
    val buffer = new ListBuffer[Page]
    for (file <- files.toList if file.endsWith("zip")) {
      val zip = new ZipFile(new File(location, file), ZipFile.OPEN_READ)
      val entries = zip.entries
      while (entries.hasMoreElements) {
        val entry = entries.nextElement
        if (entry.getName.endsWith(".xml") && entry.getName.contains("-")) {
          val xmlStream = zip.getInputStream(entry)
          val imageEntry: ZipEntry = zip.getEntry(entry.getName.replace("xml", "jpg"))
          buffer += Page.load(location + File.separator + file + File.separator + entry.getName,
            zip, entry, imageEntry)
          xmlStream.close()
        }
      }
      zip.close()
    }
    buffer.sortBy(_.id).toList
  }

  /** 
   * Import page PDF files to XML.
   * @param location The directory containing PDF files to be imported into the page XML format 
   */
  def initialImport(location: String): Unit = {
    val files = new File(location).list
    for (file <- files.toList if file.endsWith("pdf")) {
      val xml = new File(location, file.replace("pdf", "xml").replace(" ", ""))
      // TODO use separate test data (overwriting here)
      val page = Page.fromPdf(new File(location, file).getAbsolutePath)
      page.save(xml)
      println("Imported " + file)
    }
  }

  def apply(pages: List[Page]) = new Index(pages)
}