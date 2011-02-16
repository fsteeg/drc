/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.data

import scala.xml._
import java.io._
import com.quui.sinist.XmlDb
/**
 * Initial user representation: id, full name, region, reputation and XML persistence.
 * @author Fabian Steeg (fsteeg)
 */
case class User(id: String, name: String, region: String, pass: String, db:XmlDb = User.defaultDb) {
  var edits, upvotes, upvoted, downvotes, downvoted = 0
  var latestPage: String = ""
  var latestWord: Int = 0
  def reputation = (edits * 1 + upvotes * 1 + upvoted * 10) - (downvotes * 1 + downvoted * 2)
  def hasEdited { edits = edits + 1 }
  def hasUpvoted { upvotes += 1 }
  def wasUpvoted { upvoted += 1 }
  def hasDownvoted { downvotes += 1 }
  def wasDownvoted { downvoted += 1 }
  def toXml = 
    <user id={ id } name={ name } region={ region } pass={ pass } edits={ edits.toString } 
    upvotes={ upvotes.toString } upvoted={ upvoted.toString } downvotes={ downvotes.toString } 
    downvoted={ downvoted.toString } latestPage={ latestPage } latestWord={ latestWord.toString }>
    <db location={db.location} root={db.root} prefix={db.prefix}/> </user>
  def save(db:XmlDb) = db.putXml(toXml, "users", id + ".xml")
}

object User {
  private val defaultDb = XmlDb("xmldb:exist://localhost:8080/exist/xmlrpc", "/db/", "drc/")
  def withId(db:XmlDb, id: String): User =
    db.getXml("users", id + ".xml") match {
      case Some(List(xml: Elem, _*)) => User.fromXml(xml)
      case None => throw new IllegalStateException("Could not find user '%s' in DB '%s'".format(id, db))
    }

  def fromXml(xml: Node): User = {
    val db = (xml\"db")
    val u = User((xml \ "@id").text, (xml \ "@name").text, (xml \ "@region").text, (xml \ "@pass").text,
      if(db.isEmpty) defaultDb else XmlDb((db\"@location").text, (db\"@root").text, (db\"@prefix").text))
    u.edits = (xml \ "@edits").text.trim.toInt
    u.upvotes = (xml \ "@upvotes").text.trim.toInt
    u.upvoted = (xml \ "@upvoted").text.trim.toInt
    u.downvotes = (xml \ "@downvotes").text.trim.toInt
    u.downvoted = (xml \ "@downvoted").text.trim.toInt
    u.latestPage = (xml \ "@latestPage").text.trim
    val lw = (xml \ "@latestWord").text.trim
    if(lw!="") u.latestWord = lw.toInt
    u
  }
  def initialImport(db: XmlDb, folder: String): Unit = {
    for (user <- new File(folder).listFiles) db.put(user, XmlDb.Format.XML)
  }
}