/**
 * ************************************************************************************************
 * Copyright (c) 2011 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 * ***********************************************************************************************
 */
package de.uni_koeln.ub.drc.data

import scala.xml.XML
import java.io.File
import java.util.zip._
import scala.collection.mutable.ListBuffer
import com.quui.sinist.XmlDb
import com.quui.sinist.XmlDb.Format
import de.uni_koeln.ub.drc.util.PlainTextCopy
import scala.xml.Node
import de.uni_koeln.ub.drc.util.Postprocessor
import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.DBObject
import com.mongodb.casbah.MongoCollection
import org.bson.types.Binary
import com.mongodb.casbah.Imports._

/**
 * Experiments with alternative DB: MongoDB and Casbah
 */
object Casbah {

  /* Connect to default: "localhost", 27017 (install MongoDB, run 'mongod') */
  val connection: MongoConnection = MongoConnection()
  val collection: MongoCollection = connection("drc")("drc")
  /* Exist setup for moving data and benchmarks: */
  val xmlDb = XmlDb("localhost", 7777)
  val col = "drc"
  val vol = "PPN345572629_0004"

  /** Util method: load corresponding image for a page */
  def imageFor(page: Page): Array[Byte] = {
    val file = page.id.split("/").last // TODO centralize, use extractor?
    val hit = collection.findOne(MongoDBObject("id" -> file.replace(".xml", ".png")))
    hit.get.get("data").asInstanceOf[Array[Byte]]
  }
}

/**
 * Application: Copy pages and images from ExistDB to MongoDB.
 */
object PageMover {
  import Casbah._
  val volumes = List("0004") // add other volumes here
  def main(args: Array[String]): Unit = {
    collection.drop()
    for (volume <- volumes) process("PPN345572629_" + volume)
    println("Collection size: " + collection.size)
  }
  def process(volume: String): Unit = {
    val fullCollection = col + "/" + volume
    val ids = xmlDb.getIds(fullCollection).get.sorted.filter(_.endsWith(".xml")) zip
      xmlDb.getIds(fullCollection).get.sorted.filter(_.endsWith(".png"))
    for ((xmlId, imgId) <- ids) {
      val page: Page = Page.fromXml(xmlDb.getXml(fullCollection, xmlId).get(0))
      val image: Array[Byte] = xmlDb.getBin(fullCollection, imgId).get(0)
      val dbo = page.toDBObject
      val bin = MongoDBObject("id" -> imgId, "data" -> new Binary(0, image))
      collection += dbo
      collection += bin
      println("Moved %s and %s".format(xmlId, imgId))
    }
  }
}

/**
 * Application: Run benchmarks with ExistDB and MongoDB
 */
object Benchmark {
  import Casbah._
  def main(args: Array[String]) = {
    /* My results: Casbah needs about 
     * 1/3 (w/o images) to 1/6 (w/ images) of the time Exist needs */
    def benchBoth = {
      Benchmark.loadPagesWithCasbah
      println("--------------------------------------------------")
      Benchmark.loadPagesWithExist
      println("--------------------------------------------------")
    }
    (1 to 3) foreach (_ => benchBoth)
  }
  var start: Long = 0
  var end: Long = 0
  def mark(m: String): Unit = {
    end = System.currentTimeMillis; println(m + (end - start)); start = end
  }
  def loadPagesWithCasbah: List[Page] = {
    start = System.currentTimeMillis
    val xmls = collection.find(MongoDBObject("id" -> ".+xml".r))
    mark("[Casbah] Loading DB objects took: ")
    val ps = xmls.map(Page.fromDBObject(_)).toList
    mark("[Casbah] Mapping to pages took: ")
    val imgs = ps.map(Casbah.imageFor(_))
    mark("[Casbah] Loading images took: ")
    ps
  }
  def loadPagesWithExist: List[Page] = {
    start = System.currentTimeMillis
    val ids = xmlDb.getIds(col + "/" + vol).get.sorted.filter(_.endsWith(".xml"))
    mark("[Exist] Loading XML ids took: ")
    val xmls = ids.map(id => xmlDb.getXml(col + "/" + vol, id).get(0)).toList
    mark("[Exist] Loading XML entries took: ")
    val ps = xmls.map(xml => Page.fromXml(xml)).toList
    mark("[Exist] Mapping to pages took: ")
    val imgs = ps.map(p => Index.loadImageFor(db = Index.LocalDb, page = p))
    mark("[Exist] Loading images took: ")
    ps
  }
}
