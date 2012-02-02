/**************************************************************************************************
 * Copyright (c) 2010 Mihail Atanassov. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

/**
 * @author Mihail Atanassov (matana)
 */
public class ExitHandler extends AbstractHandler {
	/**
	 * The class / ExitHandler ID
	 */
	public static final String ID = ExitHandler.class.getName().toLowerCase();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return PlatformUI.getWorkbench().close();
	}

}
