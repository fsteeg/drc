/**************************************************************************************************
 * Copyright (c) 2011 Mihail Atanassov. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.facades;

/**
 * @author Mihail Atanassov (matana)
 */
public abstract class CSSSWTConstantsHelper {

	protected static final String BUNDLE_NAME = "plugin"; //$NON-NLS-1$
	private final static CSSSWTConstantsHelper IMPL;

	static {
		IMPL = (CSSSWTConstantsHelper) ImplementationLoader
				.newInstance(CSSSWTConstantsHelper.class);
	}

	/**
	 * @return Implemetation of CSSSWTConstantsHelper
	 */
	public static String getCSS() {
		return IMPL.getCSSInternal();
	}

	abstract String getCSSInternal();

}
