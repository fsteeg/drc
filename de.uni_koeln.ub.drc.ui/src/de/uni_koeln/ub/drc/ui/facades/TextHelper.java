/**************************************************************************************************
 * Copyright (c) 2011 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.facades;


/**
 * @author Fabian Steeg (fsteeg)
 */
public abstract class TextHelper {

	protected static final String BUNDLE_NAME = "plugin"; //$NON-NLS-1$
	private final static TextHelper IMPL;

	static {
		IMPL = (TextHelper) ImplementationLoader.newInstance(TextHelper.class);
	}

	/**
	 * @param text
	 *            The widget to fix for display
	 * @return The fixed string, ready to be displayed
	 */
	public static String fixForDisplay(String text) {
		return IMPL.fixForDisplayInternal(text);
	}

	abstract String fixForDisplayInternal(String text);

	/**
	 * @param text
	 *            The string to fix for saving
	 * @return The fixed string, ready to be saved
	 */
	public static String fixForSave(String text) {
		return IMPL.fixForSaveInternal(text);
	}

	abstract String fixForSaveInternal(String text);

}
