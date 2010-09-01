/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.data

import org.xmldb.api.base.XMLDBException
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
 * Wraps access to the eXist XML DB.
 * @author Fabian Steeg (fsteeg)
 */
object XmlDb {
  
  val DefaultLocation = "xmldb:exist://localhost:8080/exist/xmlrpc"
  val DefaultRoot = "/db/"
  val DefaultPrefix = ""
    
  object Format extends Enumeration {
    type Format = Value
    val XML = Value(classOf[XMLResource].getSimpleName)
    val BIN = Value(classOf[BinaryResource].getSimpleName)
  }
  
  def apply(
    location:String = XmlDb.DefaultLocation, 
    root:String = XmlDb.DefaultRoot, 
    prefix:String = XmlDb.DefaultPrefix) = new XmlDb(location, root, prefix)
}

class XmlDb(
    location:String = XmlDb.DefaultLocation, 
    root:String = XmlDb.DefaultRoot, 
    prefix:String = XmlDb.DefaultPrefix) {
  
  DatabaseManager.registerDatabase(new DatabaseImpl())

  def put(file: File, kind: XmlDb.Format.Value): Unit = {
    put(file, new File(file.getParent).getName, file.getName, kind)
  }
  
  def putXml(xml:Elem, coll:String, id:String): Unit = put(xml.toString, coll, id, XmlDb.Format.XML)
  
  def putBin(bin:Array[Byte], coll:String, id:String): Unit = put(bin, coll, id, XmlDb.Format.BIN)

  def getXml(name: String, ids: String*): Option[List[Elem]] = collection(name) match {
    case None => None
    case Some(coll) => {
      val entryIds = if (ids.size > 0) ids else getIds(name).get
      val entries = for (id <- entryIds; obj = coll.getResource(id).getContent; if obj.isInstanceOf[String]) 
        yield XML.loadString(obj.asInstanceOf[String])
      Some(entries.toList)
    }
  }

  def getBin(name: String, ids: String*): Option[List[Array[Byte]]] = collection(name) match {
    case None => None
    case Some(coll) => {
      val entryIds = if (ids.size > 0) ids else getIds(name).get
      val entries = for (id <- entryIds; obj = coll.getResource(id).getContent; if obj.isInstanceOf[Array[Byte]]) 
        yield obj.asInstanceOf[Array[Byte]]
      Some(entries.toList)
    }
  }

  def getIds(name: String): Option[List[String]] = collection(name) match {
    case None => None
    case Some(coll) => Some(List() ++ coll.listResources) // all ids
  }
  
  private def put(content: Object, collectionId: String, id: String, kind: XmlDb.Format.Value): Unit = {
    val collectionName = prefix + collectionId
    val collectionPath = root + collectionName
    var collection = DatabaseManager.getCollection(location + collectionPath)
    if (collection == null) collection = createCollection(collectionName)
    val resource = collection.createResource(id, kind.toString)
    resource.setContent(content)
    collection.storeResource(resource)
  }

  private def collection(name: String): Option[Collection] = {
    val collection = DatabaseManager.getCollection(location + root + prefix + name)
    if (collection == null) None else Some(collection)
  }

  private def createCollection(name: String): Collection = {
    try {
    DatabaseManager.getCollection(location + root)
      .getService(classOf[CollectionManagementService].getSimpleName, "1.0")
      .asInstanceOf[CollectionManagementService]
      .createCollection(name)
    } catch {
      case x: XMLDBException => throw new IllegalStateException(
      		"Could not create collection in DB at %s (%s)".format(location, x.getMessage), x)
    }
  }
  
}