/**************************************************************************************************
 * Copyright (c) 2011 Mihail Atanassov. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.facades;

import java.text.MessageFormat;

/**
 * @author Mihail Atanassov (matana)
 */
public class ImplementationLoader {

	/**
	 * @param type
	 *            The facade to get an implementation for
	 * @return The implementation
	 */
	public static Object newInstance(final Class<?> type) {
		String name = type.getName();
		Object result = null;
		try {
			result = type.getClassLoader().loadClass(name + "Impl") //$NON-NLS-1$
					.newInstance();
		} catch (Throwable t) {
			String txt = "Could not load implementation for {0}"; //$NON-NLS-1$
			String msg = MessageFormat.format(txt, new Object[] { name });
			throw new RuntimeException(msg, t);
		}
		return result;
	}
}
