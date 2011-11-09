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
import scala.xml.Elem
import de.uni_koeln.ub.drc.data.Modification
import de.uni_koeln.ub.drc.data.Index

/**
 * Simple pattern-based, non-destructive postprocessing.
 * @author Fabian Steeg
 */
object Postprocessor {

  private val col = Index.DefaultCollection

  val patterns = Map("fch" -> "sch")

  def process(volume: String, db: XmlDb): Unit = {
    for (
      id <- db.getIds(col + "/" + volume).get.filter(_.endsWith(".xml"));
      page = Page.fromXml(db.getXml(col + "/" + volume, id).get(0));
      word <- page.words;
      (k, v) <- patterns;
      form = word.history.top.form;
      if form.contains(k)
    ) {
      word.history.push(Modification(form.replace(k, v), "auto"))
      page.saveToDb(db = db)
      println(word.history)
    }
  }

}
