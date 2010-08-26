/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.data

import scala.xml.Elem
import scala.xml.XML
import javax.xml.transform.OutputKeys
import org.exist.xmldb.EXistResource
import org.xmldb.api.modules.BinaryResource
import org.xmldb.api.modules.XMLResource
import org.xmldb.api.modules.CollectionManagementService
import org.xmldb.api.DatabaseManager
import org.exist.xmldb.DatabaseImpl
import org.xmldb.api.base.Collection
import java.io.File
/**
 * Wraps access to the Exist XML DB.
 * @author Fabian Steeg (fsteeg)
 */
object Db {
  /* Requires an eXist DB running locally (http://exist.sourceforge.net/).
   * Configure in 'tools/jetty/etc/jetty.xml' to run on port '8888'.
   * Start with 'bin/startup.sh', fill with ant import (in the root of this project). */
  private val Uri = "xmldb:exist://localhost:8888/exist/xmlrpc"
  private val Root = "/db/"
  private val Drc = "drc/"
  DatabaseManager.registerDatabase(new DatabaseImpl())

  object DataType extends Enumeration {
    type DataType = Value
    val XML = Value(classOf[XMLResource].getSimpleName)
    val IMG = Value(classOf[BinaryResource].getSimpleName)
  }

  def put(file: File, kind: DataType.Value): Unit = {
    put(file, new File(file.getParent).getName, file.getName, kind)
  }
  
  def put(content:Object, collectionId: String, id: String, kind: DataType.Value): Unit = {
    val collectionName = Drc + collectionId
    val collectionPath = Root + collectionName
    var collection = DatabaseManager.getCollection(Uri + collectionPath)
    if (collection == null) collection = createCollection(collectionName)
    val resource = collection.createResource(id, kind.toString)
    resource.setContent(content)
    collection.storeResource(resource)
  }
  
  def collection(name:String):Option[Collection] = {
    val collection = DatabaseManager.getCollection(Uri + Root + Drc + name)
    if(collection==null) None else Some(collection)
  }

  def xml(name: String, ids:String*): Option[List[Elem]] = collection(name) match {
    case None => None
    case Some(coll) => Some((for(id <- ids) 
      yield XML.loadString(coll.getResource(id).getContent.asInstanceOf[String])).toList)
  }
  
  def img(name: String, ids:String*): Option[List[Array[Byte]]] = collection(name) match {
     case None => None
     case Some(coll) => Some((for(id <- ids) 
       yield coll.getResource(id).getContent.asInstanceOf[Array[Byte]]).toList)
  }
  
  def ids(name: String): Option[List[String]] = collection(name) match {
    case None => None
    case Some(coll) => Some(List() ++ coll.listResources) // all ids
  }
  
  def createCollection(name: String): Collection = {
    DatabaseManager.getCollection(Uri + Root)
      .getService(classOf[CollectionManagementService].getSimpleName, "1.0")
      .asInstanceOf[CollectionManagementService]
      .createCollection(name)
  }
}