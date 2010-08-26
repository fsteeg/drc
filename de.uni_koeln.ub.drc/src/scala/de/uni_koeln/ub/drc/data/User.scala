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
  var reputation = 0
  def hasEdited { reputation = reputation + 1}
  def hasUpvoted { reputation = reputation + 1}
  def wasUpvoted { reputation = reputation + 10}
  def hasDownvoted { reputation = reputation - 1}
  def wasDownvoted { reputation = reputation - 2}
  def toXml = <user id={id} name={name} region={region} reputation={reputation.toString} pass={pass}/>
  def save() = Db.put(toXml, "users", id+".xml", Db.DataType.XML)
}

object User {
  def withId(id:String): User = {
    val xml = Db.xml("users", id+".xml").get(0)
    User.fromXml(xml)
  }
  def fromXml(xml:Node): User = {
      val u = User((xml\"@id").text, (xml\"@name").text, (xml\"@region").text, (xml\"@pass").text)
      val trimmed = (xml\"@reputation").text.trim
      u.reputation = if(trimmed.size == 0) 0 else trimmed.toInt
      u
  }
  def initialImport(folder:String):Unit = {
    for(user<-new File(folder).listFiles) Db.put(user, Db.DataType.XML)
  }
}