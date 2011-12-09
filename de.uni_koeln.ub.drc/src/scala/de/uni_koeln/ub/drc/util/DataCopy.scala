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
 * Copy collections (non-destructive, leaves source intact).
 * @author Fabian Steeg
 */
object DataCopy {

  val fromDb = XmlDb("hydra2.spinfo.uni-koeln.de", 7777)
  val toDb = XmlDb("hydra2.spinfo.uni-koeln.de", 7777)
  val col = "drc"
  val volumes = Map("0003" -> "0014_02", "0014" -> "0014_RC")

  def main(args: Array[String]): Unit = {
    val id = "PPN345572629_"
    val prefix = col + "/" + id
    for ((fromCollection, toCollection) <- volumes) {
      process(fromCollection, toCollection, prefix)
      PlainTextCopy.process(id + toCollection, toDb)
    }
  }

  def process(fromCollection: String, toCollection: String, prefix: String): Unit = {
    for (fromId <- fromDb.getIds(prefix + fromCollection).get.sorted) {
      // include dashes to avoid renaming of pages:
      // e.g. to PPN345572629_0014_02-0014.xml, not PPN345572629_0014_02-0014_02.xml
      val newId = fromId.replace(fromCollection + "-", toCollection + "-")
      if (fromId.endsWith(".xml")) {
        val fromXml = fromDb.getXml(prefix + fromCollection, fromId).get(0)
        val newPage = Page.fromXml(fromXml, newId)
        toDb.putXml(newPage.toXml, prefix + toCollection, newId)
      } else if (fromId.endsWith("png")) {
        val img = fromDb.getBin(prefix + fromCollection, fromId).get(0)
        toDb.putBin(img, prefix + toCollection, newId)
      }
      println("Processed: " + fromId)
    }
  }

}
