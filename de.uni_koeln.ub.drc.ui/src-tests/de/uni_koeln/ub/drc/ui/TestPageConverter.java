package de.uni_koeln.ub.drc.ui;

import junit.framework.Assert;

import org.junit.Test;

import de.uni_koeln.ub.drc.ui.views.PageConverter;

public class TestPageConverter {
  
  @Test public void convertToOctopus() {
    String octopusPage = PageConverter.convert("PPN345572629_0004-0188.xml");
    Assert.assertEquals("188", octopusPage);
  }
  
  @Test public void convertToOctopusRome() {
    String octopusPage = PageConverter.convert("PPN345572629_0004-0211.xml");
    Assert.assertEquals("V", octopusPage);
  }

}
