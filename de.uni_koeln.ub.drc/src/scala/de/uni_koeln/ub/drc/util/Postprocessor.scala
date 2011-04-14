/************************************************************************************************
 * Copyright (c) 2011 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 * **********************************************************************************************/
package de.uni_koeln.ub.drc.util
import com.quui.sinist.XmlDb
import de.uni_koeln.ub.drc.data.Page
import scala.xml.Elem
import de.uni_koeln.ub.drc.data.Modification

/**
 * Simple pattern-based, non-destructive postprocessing.
 * @author Fabian Steeg
 */
object Postprocessor {

  val server = "localhost"
  val port = 7777
  val db = XmlDb(server, port)
  val volumes = List("0004", "0008", "0009", "0011", "0012", "0017", "0018", "0024", "0027")
  val patterns = Map("fch" -> "sch")

  def main(args: Array[String]): Unit = {
    for (volume <- volumes) process("PPN345572629_" + volume)
    def process(volume: String): Unit = {
      for (
        id <- db.getIds(volume).get.filter(_.endsWith(".xml"));
        page = Page.fromXml(db.getXml(volume, id).get(0), id);
        word <- page.words;
        (k, v) <- patterns;
        form = word.history.top.form;
        if form.contains(k)
      ) {
        word.history.push(Modification(form.replace(k, v), "auto"))
        page.saveToDb(db=db)
        println(word.history)
      }
    }
  }
  
}
