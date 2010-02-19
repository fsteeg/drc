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
  val history: Stack[Modification] = new Stack[Modification]
  
  def formattedHistory = history mkString "\n"
  
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
    (word\"modification").reverse.foreach( m => w.history.push(Modification.fromXml(m)) )
    w
  }
  
}
