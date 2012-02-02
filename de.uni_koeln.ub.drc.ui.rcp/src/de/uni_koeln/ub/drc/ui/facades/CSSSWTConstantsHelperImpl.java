/**************************************************************************************************
 * Copyright (c) 2011 Mihail Atanassov. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.facades;

import org.eclipse.e4.ui.css.swt.CSSSWTConstants;

/**
 * @author Mihail Atanassov (matana)
 */
public class CSSSWTConstantsHelperImpl extends CSSSWTConstantsHelper {

	@Override
	String getCSSInternal() {
		return CSSSWTConstants.CSS_CLASS_NAME_KEY;
	}

}
