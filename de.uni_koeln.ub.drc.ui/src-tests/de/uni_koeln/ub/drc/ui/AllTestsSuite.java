/*************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui;

import junit.framework.Assert;

import org.eclipse.core.runtime.Platform;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Main test suite.
 * @author Fabian Steeg (fsteeg)
 */
@RunWith( Suite.class )
@Suite.SuiteClasses( { TestDrcHeadless.class, TestPageConverter.class, TestDrcUi.class  } )
public final class AllTestsSuite {

  static final String APPLICATION_XMI = "de.uni_koeln.ub.drc.ui/Application.e4xmi";
  static final String[] PART_NAMES = { "SearchView", "EditView", "CheckView", "WordView" };

  /** Check if we are running as JUnit Plug-in test. */
  @Before
  public void setup() {
    if (!Platform.isRunning()) {
      Assert.fail("Please run as JUnit Plug-in test");
    }
  }
}
