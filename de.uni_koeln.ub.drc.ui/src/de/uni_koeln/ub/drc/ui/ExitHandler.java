/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui;

import org.eclipse.e4.workbench.ui.IWorkbench;

/**
 * Handles application exit, hooked into the menu via Application.xmi.
 * @author Fabian Steeg (fsteeg)
 */
public final class ExitHandler {
  public void execute(final IWorkbench workbench) {
    workbench.close();
  }
}