/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.data

import scala.collection.mutable.Stack
import scala.xml._
import java.io.{InputStreamReader, FileInputStream, BufferedReader}
import scala.io.Source._
import scala.collection.mutable

/**
 * Experimental representation of a Word, the basic unit of work when editing. Conceptually, it is
 * made of an original form (the original OCR result, for which we obtain coordnates in the original
 * OCR), a position in the original scan (for display in the UI; these coordinates are hard-coded
 * and absolute here, they will be obtained from the scanned PDF files and converted to relative
 * values to allow display at various image sizes) and a stack of modifications, the top of which is
 * the current form. The history has to be maintained for review and corrections.
 * 
 * @param original The original form of the word as recognized by the OCR
 * @param position The position of the word in the scanned document
 * @author Fabian Steeg (fsteeg)
 */
case class Word(original:String, position:Box) {
    
  /** A word's history is a stack of modifications, the top of which is the current form. */
  val history: Stack[Modification] = new Stack[Modification]()
  
  if( history.size == 0 ) 
      history.push(Modification(original, "OCR"))
  
  def formattedHistory = history mkString "\n"
  
  lazy val suggestions: List[String] = lexicon.sortBy(distance(_)) take 10
  
  lazy val lexicon: List[String] =
    List() ++ fromInputStream(Index.getClass.getResourceAsStream("words.txt"))("ISO-8859-1").getLines().map(_.trim)
  
  private def distance(s1: String, s2: String): Int = {
    val table = Array.ofDim[Int](s1.length + 1, s2.length + 1)
    for (i <- table.indices; j <- table(i).indices) table(i)(j) = distance(table, i, j, s1, s2)
    table(s1.length)(s2.length)
  }
  
  private def distance(table:Array[Array[Int]], i:Int, j:Int, s1:String, s2:String): Int = {
    if (i == 0) j else if (j == 0) i else {
      val del: Int = table(i - 1)(j) + 1
      val ins: Int = table(i)(j - 1) + 1
      val rep: Int = table(i - 1)(j - 1) + (if(s1(i - 1) == s2(j - 1)) 0 else 1)
      List(del, ins, rep) min
    }
  }
  
  /* Cached distances from this word to the other words in the lexicon */
  private val distances = new mutable.HashMap[String, Int]() with mutable.SynchronizedMap[String, Int]
  def distance(other: String): Int = {
    if (!distances.contains(other)) {
      distances += other -> distance(original, other)
    }
    distances(other)
  }
  
  /* Cancellable computation of edit ditances from this to the other words in the lexicon */
  private var prepDone = false
  var cancelled = false
  def prepSuggestions: Boolean = {
    if(!prepDone) {
      lexicon.foreach(if(cancelled) return false else distance(_)) // init all distances
      prepDone = true
    }
    true
  }
  
  def toXml = 
    <word> 
      <original>  { original } </original> 
      <position>  { position.toXml } </position>
      { history.map(_.toXml) }
    </word>
}

object Word {
  def fromXml(word:Node) : Word = {
    val w = Word( (word \ "original").text.trim, Box.fromXml((word \ "position" \ "box")(0)) )
    (word\"modification").reverse.foreach( m => {
        val mod =  Modification.fromXml(m)
        if(!w.history.contains(mod))
        w.history.push(mod)
    }    
    )
    w
  }
  
}
