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
/**
 * Initial user representation: id, full name, region, reputation and XML persistence.
 * @author Fabian Steeg (fsteeg)
 */
case class User(id:String, name:String, region:String, pass:String) {
  private var edits, upvotes, upvoted, downvotes, downvoted = 0
  def reputation = (edits * 1 + upvotes * 1 + upvoted * 10 ) - (downvotes * 1 + downvoted * 2)
  def hasEdited { edits = edits + 1}
  def hasUpvoted { upvotes += 1}
  def wasUpvoted { upvoted += 1}
  def hasDownvoted { downvotes += 1}
  def wasDownvoted { downvoted += 1}
  def toXml = <user 
          id={id} name={name} region={region} pass={pass} 
          edits={edits.toString} 
          upvotes={upvotes.toString} 
          upvoted={upvoted.toString} 
          downvotes={downvotes.toString} 
          downvoted={downvoted.toString}/>
  def save() = Db.put(toXml, "users", id+".xml", Db.DataType.XML)
}

object User {
  def withId(id:String): User = {
    val xml = Db.xml("users", id+".xml").get(0)
    User.fromXml(xml)
  }
  def fromXml(xml:Node): User = {
      val u = User((xml\"@id").text, (xml\"@name").text, (xml\"@region").text, (xml\"@pass").text)
      u.edits = (xml\"@edits").text.trim.toInt
      u.upvotes = (xml\"@upvotes").text.trim.toInt
      u.upvoted = (xml\"@upvoted").text.trim.toInt
      u.downvotes = (xml\"@downvotes").text.trim.toInt
      u.downvoted = (xml\"@downvoted").text.trim.toInt
      u
  }
  def initialImport(folder:String):Unit = {
    for(user<-new File(folder).listFiles) Db.put(user, Db.DataType.XML)
  }
}