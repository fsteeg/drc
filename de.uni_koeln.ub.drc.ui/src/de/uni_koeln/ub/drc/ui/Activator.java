/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * Bundle activator.
 * @author Fabian Steeg (fsteeg)
 */
public class Activator extends Plugin {

  public static final String PLUGIN_ID = "de.uni_koeln.ub.drc.ui";

  private static Activator plugin;

  public Activator() {}

  @Override public void start(final BundleContext context) throws Exception {
    super.start(context);
    plugin = this;
  }

  @Override public void stop(final BundleContext context) throws Exception {
    plugin = null;
    super.stop(context);
  }

  public static Activator getDefault() {
    return plugin;
  }

}
