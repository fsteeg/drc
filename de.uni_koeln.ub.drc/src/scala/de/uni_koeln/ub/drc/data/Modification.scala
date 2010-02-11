/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
 
package de.uni_koeln.ub.drc.data
import scala.xml._
/**
 * Represent a modification made to a word, consisting of the form the word is modified to and the
 * author of that modification (to maintain a modificaiton history for review and correction).
 * 
 * @param form The new word form for this modification
 * @param author The author of the modification // TODO will be a complex type with location, rights
 * @author Fabian Steeg (fsteeg)
 */
case class Modification(form:String, author:String) {
    def toXml = <modification> <form> {form} </form> <author> {author} </author> </modification>
}

object Modification {
    def fromXml(mod:Node) = Modification((mod\"form").text.trim, (mod\"author").text.trim)
}