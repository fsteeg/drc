/**************************************************************************************************
 * Copyright (c) 2011 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.facades;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Fabian Steeg (fsteeg)
 */
public class ScrolledCompositeHelperImpl extends ScrolledCompositeHelper {

	@Override
	void fixWrappingInternal(ScrolledComposite sc, Composite inside) {
		// nothing to do for RCP
	}

}
