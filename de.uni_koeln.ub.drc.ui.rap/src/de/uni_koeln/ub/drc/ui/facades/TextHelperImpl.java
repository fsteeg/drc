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
public class TextHelperImpl extends TextHelper {

	@Override
	String fixForDisplayInternal(String text) {
		/*
		 * Rather crude workaround for a weird RAP issue: '&' alone is displayed
		 * as '&' with blank space, if there are two '&' only one is shown (but
		 * when the text widget is selected, both appear)
		 */
		return text.replace("&", "&&"); //$NON-NLS-1$//$NON-NLS-2$
	}

	@Override
	String fixForSaveInternal(String text) {
		return text.replace("&&", "&"); //$NON-NLS-1$//$NON-NLS-2$
	}
}
