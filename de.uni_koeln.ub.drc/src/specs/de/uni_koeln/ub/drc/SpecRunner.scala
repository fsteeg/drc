/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/

package de.uni_koeln.ub.drc
import de.uni_koeln.ub.drc.util.{SpecMetsTransformer, SpecConfiguration}
import de.uni_koeln.ub.drc.data._

/**
 * Main Scala app to run the Specs. Add new Specs here.
 * @author Fabian Steeg (fsteeg) 
 **/
private[drc] object SpecRunner {
  
  def main(args : Array[String]) : Unit = {
     List(
         new SpecConfiguration,
         new SpecPage,
         new SpecWord
         //new SpecMetsTransformer // long running and unused
     ).foreach(_.execute)
  }
  
}