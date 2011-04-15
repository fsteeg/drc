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

/**
 * Merge data from one DB with positions from another DB into a third DB.
 * @author Fabian Steeg
 */
object DataMerger {

  val posDb = XmlDb("localhost", 7777)
  val dataDb = XmlDb("hydra1.spinfo.uni-koeln.de", 8080)
  val destDb = XmlDb("hydra2.spinfo.uni-koeln.de", 7777)
  val col = "drc"
  val volumes = List("0004")

  def main(args: Array[String]): Unit = {
    for (volume <- volumes) process("PPN345572629_" + volume)
    def process(volume: String): Unit = {
      val ids = posDb.getIds(col+"/"+volume).get.sorted.filter(_.endsWith(".xml")) zip
        dataDb.getIds(col+"/"+volume).get.sorted.filter(_.endsWith(".xml"))
      for ((posId, dataId) <- ids) {
        val posPage = Page.fromXml(posDb.getXml(col+"/"+volume, posId).get(0))
        val dataPage = Page.fromXml(dataDb.getXml(col+"/"+volume, dataId).get(0))
        val merged = merge(posPage, dataPage)
        merged.saveToDb(db=destDb)
      }
    }
    def merge(posPage: Page, dataPage: Page): Page = {
      dataPage.comments.foreach(posPage.comments += _)
      dataPage.tags.foreach(posPage.tags += _)
      for (
        (posWord, dataWord) <- posPage.words zip dataPage.words
      ) {
        dataWord.history.reverse.tail.foreach(posWord.history.push(_))
        printf("pos: %s, data: %s\n", posWord, dataWord)
      }
      posPage
    }
  }

}
