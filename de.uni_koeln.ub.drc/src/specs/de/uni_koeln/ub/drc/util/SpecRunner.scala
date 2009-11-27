package de.uni_koeln.ub.drc.util

/**
 * Main Scala app to run the Specs. Add new Specs here.
 * @author Fabian Steeg (fsteeg) 
 **/
private[util] object SpecRunner {
  def main(args : Array[String]) : Unit = {
     List(
         new ConfigurationSpec,
         new MetsTransformerSpec
     ).foreach(_.execute)
  }
}