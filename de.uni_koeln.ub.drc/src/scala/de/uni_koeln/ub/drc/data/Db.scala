/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.data

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

  private val Uri = "xmldb:exist://localhost:8080/exist/xmlrpc"
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

  def get(name: String, ids:String*): List[Object] = {
    val collection = DatabaseManager.getCollection(Uri + Root + Drc + name)
    collection.setProperty(OutputKeys.INDENT, "no")
    if(ids.size==0) List() ++ collection.listResources // all ids
    else (for(id <- ids) yield collection.getResource(id).getContent).toList
  }
  
  def createCollection(name: String): Collection = {
    DatabaseManager.getCollection(Uri + Root)
      .getService(classOf[CollectionManagementService].getSimpleName, "1.0")
      .asInstanceOf[CollectionManagementService]
      .createCollection(name)
  }
}