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

/**
 * Add ID attributes to pages in the DB.
 * @author Fabian Steeg
 */
object IdUpdater {

  val localDb = XmlDb("localhost", 7777)
  //val stageDb = XmlDb("hydra2.spinfo.uni-koeln.de", 7777)
  //val prodDb = XmlDb("hydra1.spinfo.uni-koeln.de", 8080)
  val col = "drc"
  val volumes = List("0004","0008","0009","0011","0012","0017","0018","0024","0027")

  def main(args: Array[String]): Unit = {
    for (volume <- volumes) process("PPN345572629_" + volume, localDb)
    def process(volume: String, db: XmlDb): Unit = {
      val ids = db.getIds(col + "/" + volume).get.sorted.filter(_.endsWith(".xml"))
      for (id <- ids) {
        val words: List[Word] = ((db.getXml(col + "/" + volume, id).get(0) \ "word") map (Word.fromXml(_))).toList
        val page = new Page(words, id)
        page.saveToDb(db = db)
        println("Updated %s".format(id))
      }
    }
  }

}
