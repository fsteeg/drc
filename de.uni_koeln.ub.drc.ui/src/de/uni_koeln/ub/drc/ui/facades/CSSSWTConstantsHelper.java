/**************************************************************************************************
 * Copyright (c) 2010 Mihail Atanassov. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.facades;

import de.uni_koeln.ub.drc.ui.util.ImplementationLoader;

public abstract class CSSSWTConstantsHelper {

	protected static final String BUNDLE_NAME = "plugin"; //$NON-NLS-1$
	public final static CSSSWTConstantsHelper IMPL;

	static {
		IMPL = (CSSSWTConstantsHelper) ImplementationLoader.newInstance(CSSSWTConstantsHelper.class);
	}
	
	public static String getCSS() {
		return IMPL.getCSSInternal();
	}

	abstract String getCSSInternal();
	
}
