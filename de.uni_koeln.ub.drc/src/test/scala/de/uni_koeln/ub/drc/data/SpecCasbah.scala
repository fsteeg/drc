/**
 * Copyright (c) 2011 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 */
package de.uni_koeln.ub.drc.data

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.matchers.ShouldMatchers
import scala.xml._
import org.scalatest.FlatSpec
import com.mongodb.casbah.Imports._
import scala.collection.JavaConversions._
import org.bson.types.BasicBSONList

/**
 * Test MongoDB access using Casbah (http://api.mongodb.org/scala/casbah/2.1.5.0/).
 * @author Fabian Steeg
 */
@RunWith(classOf[JUnitRunner])
class SpecCasbah extends FlatSpec with ShouldMatchers {

  // Connect to default: "localhost", 27017 (install MongoDB, run 'mongod')
  val connection: MongoConnection = MongoConnection()
  val collection: MongoCollection = connection("test")("test")
  collection.drop()

  val item1 = MongoDBObject(
    "foo" -> "bar1",
    "x" -> List(Map("y" -> "nested").asDBObject),
    "pie" -> 3.14,
    "spam" -> "eggs")

  val item2 = MongoDBObject(
    "foo" -> "bar2",
    "but" -> "y",
    "other" -> 3.14,
    "stuff" -> "eggs")

  collection += item1
  collection += item2

  "Casbah" should "allow access to all DB items" in {
    for (item <- collection) println("DB item: " + item)
  }

  it should "allow access to specific DB items" in {
    val i1: MongoDBObject = collection.findOne(item1).get
    i1("foo") should equal { "bar1" }
    i1("x").asInstanceOf[java.util.List[_]].get(0).asInstanceOf[DBObject].get("y") should equal { "nested" }
    i1("pie") should equal { 3.14 }
    i1("spam") should equal { "eggs" }
  }

  it should "be able to convert maps to db objects" in {
    val v: DBObject = Map("foo" -> "bar", "a" -> "b").asDBObject
  }

  it should "allow access to all DB items containing a specific key" in {
    val q: DBObject = "foo" $exists true
    val fooHolders = for (x <- collection.find(q)) yield x
    fooHolders.size should equal { 2 }
  }

  it should "allow access to all DB items containing a specific key and value" in {
    val q: DBObject = "foo" $ne "bar1"
    val result = for (x <- collection.find(q)) yield x
    result.size should equal { 1 }
  }

  val userFromXml = User.fromXml(XML.loadFile("res/tests/auto.xml"))
  val pageFromXml = Page.fromXml(XML.loadFile("res/tests/PPN345572629_0004-0007.xml"))

  "A mapper for our domain objects" should "convert users to maps" in {
    val userFromMap = User.fromDBObject(userFromXml.toDBObject)
    userFromMap should equal { userFromXml }
  }

  it should "store users as DBObjects" in {
    val userFromXml = User.fromXml(XML.loadFile("res/tests/auto.xml"))
    val dbObject: DBObject = userFromXml.toDBObject
    collection.drop()
    collection += dbObject
    val fromDb = collection.findOne(dbObject).get
    val userFromMap = User.fromDBObject(fromDb)
    userFromMap should equal { userFromXml }
  }

  it should "convert pages to maps" in {
    val pageFromMap = Page.fromDBObject(pageFromXml.toDBObject)
    pageFromMap should equal { pageFromXml }
  }

  it should "store pages as DBObjects" in {
    val pageFromMap = Page.fromDBObject(pageFromXml.toDBObject)
    val objectForDb = pageFromXml.toDBObject
    collection.drop()
    collection += objectForDb
    val objectFromDb: DBObject = collection.findOne(objectForDb).get
    val pageFromDb = Page.fromDBObject(objectFromDb)
    pageFromDb.words.size should equal { pageFromXml.words.size }
    pageFromDb.tags.size should equal { pageFromXml.tags.size }
    pageFromDb.comments.size should equal { pageFromXml.comments.size }
    pageFromDb.status.size should equal { pageFromXml.status.size }
    // TODO: preserve order of words in retrieved pages
    // pageFromDb should equal { pageFromXml }
  }

}