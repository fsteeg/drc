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
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import Configuration._
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
/**
 * @see Configuration
 * @author Fabian Steeg (fsteeg)
 */
@RunWith(classOf[JUnitRunner])
private[drc] class SpecConfiguration extends Spec with ShouldMatchers {

  describe("The Configuration") {
    val entries = List(Romafo, Cdm, Res)
    it("should point to existing directories") { entries.foreach(new File(_).exists) }
    it("should point to non-empty directories") { entries.foreach(new File(_).list.length > 0) }
  }
  
}