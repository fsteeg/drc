/**
 * **********************************************************************************************
 * Copyright (c) 2011 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 * *********************************************************************************************
 */
package de.uni_koeln.ub.drc.util

import com.quui.sinist.XmlDb
import de.uni_koeln.ub.drc.data.Page
import de.uni_koeln.ub.drc.data.Word
import scala.xml.Node
import scala.xml.PrettyPrinter
import de.uni_koeln.ub.drc.data.Index

/**
 * Copy latest version of pages as plain text to a different collection (for index and KWIC).
 * @author Fabian Steeg
 */
object PlainTextCopy {

  private val col = Index.DefaultCollection
  val suffix = "-plain"

  def process(volume: String, db: XmlDb): Unit = {
    val ids = db.getIds(col + "/" + volume).get.sorted.filter(_.endsWith(".xml"))
    for (id <- ids) {
      val words: List[Word] = ((db.getXml(col + "/" + volume, id).get(0) \ "word") map (Word.fromXml(_))).toList
      val page = new Page(words, id)
      saveToDb(page, col + suffix, volume, db)
      println("Copied %s".format(id))
    }
  }

  def saveToDb(page: Page, collection: String, volume: String, db: XmlDb): Node = {
    val c = collection + "/" + volume
    val root = <page id={ page.id }>{ page.toText("\n") }</page>
    val formatted = format(root)
    db.putXml(root, c, page.id.split("/").last)
    root
  }

  private def format(root: Node) = {
    val formatted = new StringBuilder
    new PrettyPrinter(120, 2).format(root, formatted)
    formatted
  }

}
