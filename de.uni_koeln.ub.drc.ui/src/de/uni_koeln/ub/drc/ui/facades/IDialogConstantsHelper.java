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
public abstract class IDialogConstantsHelper {

	protected static final String BUNDLE_NAME = "plugin"; //$NON-NLS-1$
	private final static IDialogConstantsHelper IMPL;

	static {
		IMPL = (IDialogConstantsHelper) ImplementationLoader
				.newInstance(IDialogConstantsHelper.class);
	}

	/**
	 * @return YES label
	 */
	public static String getYesLabel() {
		return IMPL.getYesLabelInternal();
	}

	/**
	 * @return NO label
	 */
	public static String getNoLabel() {
		return IMPL.getNoLabelInternal();
	}

	abstract String getNoLabelInternal();

	abstract String getYesLabelInternal();

}
