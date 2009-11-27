package de.uni_koeln.ub.drc.util

import scala.xml._
import java.io._
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import Configuration._

/**
 * @see Configuration
 * @author Fabian Steeg (fsteeg)
 */
private[util] class ConfigurationSpec extends Spec with ShouldMatchers {

  describe("The Configuration") {
    val entries = List(Romafo, Cdm, Res)
    it("should point to existing directories") { entries.foreach(new File(_).exists) }
    it("should point to non-empty directories") { entries.foreach(new File(_).list.length > 0) }
  }
  
}