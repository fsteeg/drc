/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.handlers;

import java.lang.reflect.InvocationTargetException;

import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.IDisposable;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.workbench.ui.Persist;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Handles document saving, hooked into the menu via Application.xmi.
 * @author Fabian Steeg (fsteeg)
 */
public final class SaveHandler {

  public boolean canExecute(@Optional @Named( IServiceConstants.ACTIVE_PART ) MDirtyable dirtyable) {
    return dirtyable.isDirty();
  }
  @Execute
  public void execute(IEclipseContext context, @Optional final IStylingEngine engine,
      @Optional @Named( IServiceConstants.ACTIVE_SHELL ) final Shell shell,
      @Optional @Named( IServiceConstants.ACTIVE_PART ) final MContribution contribution)
      throws InvocationTargetException, InterruptedException {

    final IEclipseContext pmContext = context.createChild();
    ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
    dialog.open();
    applyDialogStyles(engine, dialog.getShell());
    
    dialog.run(true, true, new IRunnableWithProgress() {
      public void run(final IProgressMonitor monitor) throws InvocationTargetException,
          InterruptedException {
        pmContext.set(IProgressMonitor.class.getName(), monitor);
        Object clientObject = contribution.getObject();
        ContextInjectionFactory.invoke(clientObject, Persist.class, //$NON-NLS-1$
            pmContext, null);
      }
    });

    if (pmContext instanceof IDisposable) {
      ((IDisposable) pmContext).dispose();
    }
  }
  
  static void applyDialogStyles(final IStylingEngine engine, final Control control) {
    if (engine != null) {
      Shell shell = control.getShell();
      if (shell.getBackgroundMode() == SWT.INHERIT_NONE) {
        shell.setBackgroundMode(SWT.INHERIT_DEFAULT);
      }
      engine.style(shell);
    }
  }
  
}