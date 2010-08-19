/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.data

import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import com.novocode.squery.session._
import com.novocode.squery.session.Database.threadLocalSession
import Tables._
/**
 * @author Fabian Steeg
 */
@RunWith(classOf[JUnitRunner])
class SpecQuery extends Spec with ShouldMatchers {
  val mem = "jdbc:h2:mem:test1"
  val ser = "jdbc:h2:tcp://localhost/~/test"
  val db = Database.forURL(ser, driver = "org.h2.Driver")

  import com.novocode.squery.combinator.basic.BasicDriver.Implicit._
   val q1 = for (u <- SampleUsers if u.first === "Stefan") yield u
  db withSession {
//    (SampleUsers.ddl ++ Orders.ddl) create
    
    SampleUsers insert (0, "Fabian", None)
    SampleUsers insert (1, "Stefan", None)
   }
  db withSession {
   
    println(q1.list())
  }
}