/**************************************************************************************************
 * Copyright (c) 2010 Mihail Atanassov. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui;

import junit.framework.Assert;

import org.junit.Test;

import de.uni_koeln.ub.drc.ui.views.PageConverter;

/**
 * Tests for the {@link PageConverter} class.
 * @author Mihail Atanassov <saeko.bjagai@googlemail.com>
 */
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
