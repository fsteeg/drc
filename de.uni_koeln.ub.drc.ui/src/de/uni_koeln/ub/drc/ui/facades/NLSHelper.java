/**************************************************************************************************
 * Copyright (c) 2011 Mihail Atanassov. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.facades;

import de.uni_koeln.ub.drc.ui.Messages;

/**
 * @author Mihail Atanassov (matana)
 */
public abstract class NLSHelper {

	protected static final String BUNDLE_NAME = "plugin"; //$NON-NLS-1$
	private final static NLSHelper IMPL;

	static {
		IMPL = (NLSHelper) ImplementationLoader.newInstance(NLSHelper.class);
	}

	/**
	 * @return The Messages implementation
	 */
	public static Messages getMessages() {
		return (Messages) IMPL.getMessagesInternal(Messages.class);
	}

	protected abstract Object getMessagesInternal(final Class<?> clazz);

}
