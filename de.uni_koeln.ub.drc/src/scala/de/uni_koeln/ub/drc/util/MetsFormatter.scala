package de.uni_koeln.ub.drc.util
import java.io._
import scala.xml._
import Configuration._

/**
 * Formats all the METS metadata files to make them useable in the Eclipse XML editor.
 * @author Fabian Steeg (fsteeg)
 **/
private[util] object MetsFormatter {
    
  def main(args : Array[String]) : Unit = {
    new File(Romafo).list.filter(!_.startsWith(".")).foreach(format(_))
  }
  
  def format(s: String){
    require(!s.startsWith("."))
    val xmlFile = new File(Romafo+"/"+s, s+".xml")
    println(xmlFile)
    val xml = XML.load(new FileReader(xmlFile))
    val formatted = new StringBuilder()
    new PrettyPrinter(120, 2).format(xml, formatted)
    val writer = new FileWriter(xmlFile)
    writer.write(formatted.toString); writer.close
  }
  
}
