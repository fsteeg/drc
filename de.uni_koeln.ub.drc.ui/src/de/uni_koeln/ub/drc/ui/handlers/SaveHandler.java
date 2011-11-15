/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import de.uni_koeln.ub.drc.ui.views.EditView;

/**
 * Handles document saving, hooked into the menu via Application.xmi.
 * 
 * @author Fabian Steeg (fsteeg)
 */
public final class SaveHandler extends AbstractHandler {
	/**
	 * The class / SaveHandler ID
	 */
	public static final String ID = SaveHandler.class.getName().toLowerCase();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil
				.getActiveWorkbenchWindow(event);
		EditView ev = (EditView) activeWorkbenchWindow.getActivePage()
				.findView(EditView.ID);
		if (ev != null && ev.isDirty())
			ev.doSave(new NullProgressMonitor());
		return ev;
	}

}