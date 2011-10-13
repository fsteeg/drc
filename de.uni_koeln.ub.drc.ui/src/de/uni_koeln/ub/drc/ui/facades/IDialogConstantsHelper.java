/**************************************************************************************************
 * Copyright (c) 2010 Mihail Atanassov. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.facades;

import de.uni_koeln.ub.drc.ui.util.ImplementationLoader;

public abstract class IDialogConstantsHelper {
	
	protected static final String BUNDLE_NAME = "plugin"; //$NON-NLS-1$
	public final static IDialogConstantsHelper IMPL;

	static {
		IMPL = (IDialogConstantsHelper) ImplementationLoader.newInstance(IDialogConstantsHelper.class);
	}
	
	public static String getYesLabel() {
		return IMPL.getYesLabelInternal();
	}
	
	public static String getNoLabel() {
		return IMPL.getNoLabelInternal();
	}

	abstract String getNoLabelInternal();

	abstract String getYesLabelInternal();

}
