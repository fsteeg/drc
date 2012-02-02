/**************************************************************************************************
 * Copyright (c) 2011 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.facades;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @author Fabian Steeg (fsteeg)
 */
public class ScrolledCompositeHelperImpl extends ScrolledCompositeHelper {

	@Override
	void fixWrappingInternal(final ScrolledComposite sc, final Composite inside) {
		sc.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				sc.setMinHeight(inside.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
				sc.getParent().layout();
			}
		});
	}

}
