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
 * Initial user representation: id, full name, region and XML persistence.
 * @author Fabian Steeg (fsteeg)
 */
case class User(id:String, name:String, region:String) {
  def toXml = <user id={id} name={name} region={region}/>
}

object User {
  def save(location: String, users:User*) = {
    for(u <- users) XML.save(location + "/" + u.id + ".xml", u.toXml, "UTF-8")
  }
  private def users(location:String): Map[String, User] = { // TODO load once only
    var map: Map[String,User] = Map()
    for(file <- new File(location).list.toList if file.endsWith("xml") ) {
      val user = User.fromXml(XML.loadFile(new File(location + "/" + file)))
      map += user.id -> user
    }
    map
  }
  def withId(id:String, location:String): User = users(location)(id)
  def fromXml(xml:Node): User = User((xml\"@id").text, (xml\"@name").text, (xml\"@region").text)
}