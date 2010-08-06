/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/

package de.uni_koeln.ub.drc.data
import scala.xml._
import scala.reflect._
import scala.annotation.target._
import sjson.json._
/**
 * A box represents the position of a word in the original scanned document.
 * 
 * @param x The lower left corner's x position
 * @param y The lower left corner's y position
 * @param width The width of the box
 * @param height The height of the box
 * @author Fabian Steeg (fsteeg)
 */
@BeanInfo case class Box(x:Int, y:Int, width:Int, height:Int) {
  private def this() = this(0, 0, 0, 0) // for scouch
  override def toString = "box with x=%s, y=%s, width=%s, height=%s".format(x, y, width, height)
  def toXml = 
      <box x={x.toString} y={y.toString} width={width.toString} height={height.toString}/>
}

object Box {
  def fromXml(box: Node): Box = {
      Box(
          (box\"@x").text.trim.toInt, 
          (box\"@y").text.trim.toInt, 
          (box\"@width").text.trim.toInt, 
          (box\"@height").text.trim.toInt
      )
  }
}