/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.handlers;

import java.util.Collections;

import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;

/**
 * Handles application exit, hooked into the menu via Application.xmi.
 * @author Fabian Steeg (fsteeg)
 */
public final class ExitHandler {
  @Inject
  private ECommandService commandService;
  @Inject
  private EHandlerService handlerService;

  @Execute
  public void execute(final IWorkbench workbench) {
    ParameterizedCommand saveCommand = commandService.createCommand("page.save",
        Collections.EMPTY_MAP);
    handlerService.executeHandler(saveCommand);
    workbench.close();
  }
}