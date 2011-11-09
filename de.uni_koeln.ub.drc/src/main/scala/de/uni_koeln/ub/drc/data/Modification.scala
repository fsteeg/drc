/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
 
package de.uni_koeln.ub.drc.data
import scala.xml._
import scala.collection.mutable.ListBuffer
/**
 * Represent a modification made to a word, consisting of the form the word is modified to and the
 * author of that modification (to maintain a modificaiton history for review and correction), as
 * well as a score (the absolute value of up- and downvotes).
 * 
 * @param form The new word form for this modification
 * @param author The ID of the modification author
 * @author Fabian Steeg (fsteeg)
 */
case class Modification(form:String, author:String) {
    var date = System.currentTimeMillis
    var score = 0
    var voters = scala.collection.mutable.Set[String]()
    def upvote(voter:String) { guard(voter); score = score + 1; voters += voter }
    def downvote(voter:String) { guard(voter); score = score - 1; voters += voter }
    private def guard(voter:String) = {
        if(voters.contains(voter)) throw new IllegalArgumentException(voter + " has already voted")
    }
    def toXml = 
        <modification form={form} author={author} score={score.toString} date={date.toString}> 
            <voters> { voters.map((id:String) => <voter name={id}/>) } </voters>
        </modification>
    
}

object Modification {
    def fromXml(mod:Node) = {
        val m = Modification( (mod\"@form").text.trim, (mod\"@author").text.trim )
        val trimmed = (mod\"@score").text.trim
        m.score = if(trimmed.size == 0) 0 else trimmed.toInt
        m.voters = scala.collection.mutable.Set[String]() ++ 
            (mod \\ "voter").map((n:Node) => (n\"@name").text.trim)
        m.date = (mod\"@date").text.toLong
        m
    }
}