/**************************************************************************************************
 * Copyright (c) 2011 Mihail Atanassov. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.facades;

import org.eclipse.rwt.RWT;

import de.uni_koeln.ub.drc.ui.Messages;

/**
 * @author Mihail Atanassov (matana)
 */
public class NLSHelperImpl extends NLSHelper {

	@Override
	protected Object getMessagesInternal(final Class<?> clazz) {
		return RWT.NLS.getISO8859_1Encoded(BUNDLE_NAME, Messages.class);
	}

}
