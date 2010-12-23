/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.util

import scala.xml._
import java.io._

import scala.xml._
import scala.collection.immutable._
import java.io.FileReader
import java.lang.StringBuilder
import scala.util.matching.Regex

import Configuration._

/** 
 * Early experimentation: convert METS metadata (XML) for scans at http://www.digizeitschriften.de/main/dms/toc/?IDDOC=5570 
 * (see http://www.digizeitschriften.de/main/dms/toc/?IDDOC=5570 for format info in German) to the ContentDM input format (TSV). 
 * @see MetsTransformerSpec
 * @author Fabian Steeg (fsteeg)
 **/
private[util] object Count extends Enumeration {
  type Count = Value
  val Label = Value
  val File = Value
}

case class Chapter(volume:Int, number:Int, title:String) extends Ordered[Chapter] {
  def compare(that:Chapter) = 
    if(this.volume==that.volume) this.number compare that.number else this.volume compare that.volume
  override def toString = "Chapter %s: %s".format(if (number == Integer.MAX_VALUE) "X" else number, title)
}

private[util] class MetsTransformer(xml:Elem) {
  
  def this(name:String, db: com.quui.sinist.XmlDb) = this(db.getXml("PPN345572629", name).get(0))
  
  private val mods = xml\"dmdSec"\"mdWrap"\"xmlData"\"mods"
  private val loc = (mods\"location"\"url").text.trim
  private val num = (mods\"part"\"detail"\"number").text.trim
  private val date = (mods\"originInfo"\"dateIssued").text.trim
  private val place = (mods\"originInfo"\"place"\"placeTerm").text.trim
  private val pub = (mods\"originInfo"\"publisher").text.trim
  
  /* Maps used when generating the output in the transform method: */
  private val log: (String, Map[String, (String, String)]) = buildLogMap()
  private val fullTitle = log._1
  private var fileMap: Map[String, String] = buildFileMap // file -> physID, e.g. 205 -> phys206
  private var logMap: Map[String,(String, String)] = log._2 // logID -> chapter, e.g. log16 -> (Chapter 8, Canzun)
  private var physMap: Map[String, String] = buildPhysMap // physID -> label, e.g. phys562 -> 549
  private var linkMap: Map[String, String] = buildLinkMap // physID -> logID, e.g. phys562 -> log67
  
  private[util] def label(file:Int) : String = physMap(fileMap(file.toString)) // result is String, can be "XVI"
  
  private[util] def chapter(page:Int, mode:Count.Value = Count.File): Chapter = {
    lazy val labelMap = physMap.map(_.swap)
    val chapter = try { 
      logMap(linkMap( mode match {
        case Count.Label => labelMap(page.toString)
        case Count.File => fileMap(page.toString)
    }))
    } catch {
      case nse:NoSuchElementException => ("Unknown", "Unknown")
    }
    val number = if(chapter._1.contains("Chapter")) chapter._1.split(" ")(1).toInt else Integer.MAX_VALUE
    Chapter(4, number, chapter._2) // TODO get volume from initial metadata location, or init with volume number
  }
  
  private[util] def transform(): String = {
    val builder = new StringBuilder()
    val lines : List[String] = List()
    
    builder.append(tabbed(List("CDM_LVL","CDM_LVL_NAME","Titel", "Quelle", "Publikation", "Ausgabe", "Autor", "Jahr", "Verlag", "Ort", "Typ", "Dateiformat", "Bemerkungen", "Dateiname")))
    builder.append(tabbed(List("", fullTitle, fullTitle, loc, "Romanische Forschungen", num, "C. Decurtins", date, pub, place, "Image", "image/tif")))
    builder.append(tabbed(List("0", fullTitle, "Titelblatt", "", "", "", "", "", "", "", "", "", "o", "00000005.tif")))

    xml\"fileSec"\"fileGrp"\"file" foreach{ (file) =>
      val fileId = file\"@ID"
      // The following line is not working with the current Scala 2.8 nightlies, work-around via regex below
      // val fileUrlOrig = (file\"FLocat"\"@{http://www.w3.org/1999/xlink}href").toString
      val fileUrl = attribute("href", (file\"FLocat"))
      if(fileUrl endsWith ".tif") {
        /** For each valid file, we get the details... */
        xml\"structMap"\"div"\"div" foreach { (div) =>
          div\"fptr" foreach { (fptr) =>
            if(fptr\"@FILEID" equals fileId) {
              /** Get the physical ID for the file... */
              val physId = (div\"@ID").text
              builder.append(processStructLinks(physId, fileUrl))
            }
          }
        }
      }
    }
    
    def processStructLinks(physId: String, fileUrl: String) = {
      if(linkMap.contains(physId)){
        val logId = linkMap(physId)
        if(logMap.contains(logId)) {
          val vals = logMap(logId)
          /** Print the line in the tabstop-sep. file for one scanned file: */
          val fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1)
          tabbed(List("1", vals._1 + ": " + vals._2, physMap(physId) , "", "", "", "", "", "", "", "", "", "o", fileName))
        }
      }
    }
    
    builder.toString
    
  }
  
  private def attribute(s:String, n:NodeSeq): String = {
    val regex = new Regex("""<METS:.* xlink:""" + s + """="(.*?)".*>""")
    val regex(fileUrl) = n.toString.split("\n")(0)
    fileUrl
  }
   
  private def tabbed(items:List[String]): String = {
    val builder = new StringBuilder()
    for(item <- items) {
      if(item.trim.size > 0) builder.append("\"" + item + "\"")
      builder.append("\t")
    }
    builder.append("\n").toString
  }
  
  private def buildLogMap(): (String, Map[String, (String, String)]) = {
    /* Map logical IDs to chapter numbers and labels: */
    var logMap: Map[String,(String,String)] = Map()
    var fullTitle = ""
    var chapterCount = 0
    xml\"structMap"\"div"\"div"\"div" foreach { (div) =>
      /* Process on this level: */
      process(div)
      /* And one level below: */
      div\"div" foreach { process(_) }
    }
    
    def process(div:Node) = {
      val parentLabel = (div\"@LABEL").toString.trim
      var lastLabel = ""
      div\"div" foreach{ (subdiv) =>
        val label : String = (subdiv\"@LABEL").toString.trim
        if(parentLabel.contains("Chrestomathie")) {
          fullTitle = parentLabel
          val typeLabel : String = (subdiv\"@TYPE").toString.trim
          if(label!=lastLabel && typeLabel.contains("Chapter")) chapterCount += 1
          lastLabel=label
          val id = if(typeLabel.contains("Chapter")) " " + chapterCount else ""
          logMap += (subdiv\"@ID").text -> (typeLabel+id, label)
        }
      }
    }
    
    (fullTitle, logMap)
    
  }
  
  private def buildPhysMap() = {
    /* Map physical IDs to page numbers: */
    var physMap:Map[String,String] = Map()
    xml\"structMap"\"div"\"div" foreach { (div) =>
      val id = (div\"@ID").text.trim
      var page = (div\"@ORDERLABEL").text.trim
      if(page.length == 0) page = (div\"@ORDER").text.trim
      if(id.length() > 0 && page.length() > 0) physMap += id -> page
    }
    physMap
  }
  
  private def buildFileMap() = {
    /* Map file IDs to physical IDs: */
    var fileMap:Map[String,String] = Map()
    xml\"structMap"\"div"\"div" foreach { (div) =>
      val id = (div\"@ID").text.trim
      var page = (div\"@ORDER").text.trim
      if(id.length() > 0 && page.length() > 0) {
        fileMap += page -> id
      }
    }
    fileMap
  }
  
  private def buildLinkMap = {
    /* Map physical IDs to logical IDs: */
    var linkMap:Map[String,String] = Map()
    xml\"structLink"\"smLink" foreach { (link) =>
      /* physId -> logId */
      linkMap += attribute("to", link) -> attribute("from", link)
    }
    linkMap
  }
  
}