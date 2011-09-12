/**
 * ************************************************************************************************
 * Copyright (c) 2011 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 * ***********************************************************************************************
 */

import scala.xml._
import java.io._
import org.scalatest.Spec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import play.test.UnitTest
import de.uni_koeln.ub.drc.data.Index
import controllers.Application._
/**
 * @see controllers.Application
 * @author Fabian Steeg (fsteeg)
 */
@RunWith(classOf[JUnitRunner])
class KwicSpec extends UnitTest with Spec with ShouldMatchers {
  describe("The exist KWIC processing") {
    it("should parse an exist result with existing metadata") {
      val response: Elem =
        <tr xmlns:exist="http://exist.sourceforge.net/NS/exist">
          <td class="previous"> 90 </td>
          <td class="hi">
            <a href="PPN345572629_0036-0116.xml">Catechismus</a>
          </td>
          <td class="following"> da Peder anisius Chià chiaffa ais ün Sa ...</td>
        </tr>
      Hit(
        "Catechismus",
        "90",
        "da Peder anisius Chià chiaffa ais ün Sa ...",
        "PPN345572629_0036-0116.xml",
        "0036",
        "X, 3.",
        "90 (RF)",
        "http://%s:%s/exist/rest/db/drc-plain/PPN345572629_0036/PPN345572629_0036-0116.xml" format(server,port),
        "http://%s:%s/exist/rest/db/drc/PPN345572629_0036/PPN345572629_0036-0116.png" format(server,port)) should equal(parse(response))
    }
    it("should parse an exist result without metadata") {
      val response: Elem =
        <tr xmlns:exist="http://exist.sourceforge.net/NS/exist">
          <td class="previous">
            ... r in plaun, Per siu êr'zerclare. Ais,
          </td>
          <td class="hi">
            <a href="PPN345572629_0014-0109.xml">cha</a>
          </td>
          <td class="following"> vus mamma savesses, Sch'emprestassas il ...</td>
        </tr>
      Hit(
        "cha",
        "... r in plaun, Per siu êr'zerclare. Ais,",
        "vus mamma savesses, Sch'emprestassas il ...",
        "PPN345572629_0014-0109.xml",
        "0014",
        "XIV",
        "n/a",
        "http://%s:%s/exist/rest/db/drc-plain/PPN345572629_0014/PPN345572629_0014-0109.xml" format(server,port),
        "http://%s:%s/exist/rest/db/drc/PPN345572629_0014/PPN345572629_0014-0109.png" format(server,port)) should equal(parse(response))
    }
    it("should sort hits by their volume") {
      val hits = List(
          Hit(volume = "0014", mappedVolume = "XIV"),
          Hit(volume = "0033", mappedVolume = "XIII"),
          Hit(volume = "0038", mappedVolume = "XII"),
          Hit(volume = "0037", mappedVolume = "XI"),
          Hit(volume = "0036", mappedVolume = "X, 3."),
          Hit(volume = "0035", mappedVolume = "X, 1. - 3."),
          Hit(volume = "0027", mappedVolume = "IX"),
          Hit(volume = "0024", mappedVolume = "VIII"),
          Hit(volume = "0018", mappedVolume = "VII"),
          Hit(volume = "0017", mappedVolume = "VI"),
          Hit(volume = "0012", mappedVolume = "V"),
          Hit(volume = "0030", mappedVolume = "IV"),
          Hit(volume = "0011", mappedVolume = "II, 2. - 3."),
          Hit(volume = "0009", mappedVolume = "II, 1."),
          Hit(volume = "0008", mappedVolume = "I, 2. - 3."),
          Hit(volume = "0004", mappedVolume = "I, 1."))
       hits.reverse should equal(hits.sorted)
    }
  }
}