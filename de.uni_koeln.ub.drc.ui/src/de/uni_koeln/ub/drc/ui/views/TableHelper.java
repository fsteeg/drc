/**************************************************************************************************
 * Copyright (c) 2011 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

import de.uni_koeln.ub.drc.ui.DrcUiActivator;

/**
 * Helper methods for working with tables.
 * 
 * @author Fabian Steeg (fsteeg)
 */
public class TableHelper {
	private TableHelper() {
		/* Static helper class. */
	}

	static void clearWidgets(Table table) {
		TableItem[] items = table.getItems();
		for (int i = 0; i < items.length; i++) {
			if (!items[i].isDisposed()) {
				Object data = items[i].getData();
				if (data != null && data instanceof Widget[]) {
					Widget[] widgets = (Widget[]) data;
					for (Widget widget : widgets) {
						if (widget != null) {
							widget.dispose();
						}
					}
				}
			}
		}
	}

	static Link insertLink(final Table table, final TableItem item,
			final String author, int index) {
		if (!author.equals(DrcUiActivator.OCR_ID)) {
			TableEditor editor = new TableEditor(table);
			Link link = new Link(table, SWT.NONE);
			link.setText("<a>" + item.getText(index) + "</a>"); //$NON-NLS-1$//$NON-NLS-2$
			item.setText(index, ""); //$NON-NLS-1$
			link.pack();
			editor.minimumWidth = link.getSize().x;
			editor.minimumHeight = link.getSize().y;
			editor.horizontalAlignment = SWT.LEFT;
			editor.setEditor(link, item, index);
			addLinkListener(author, link);
			return link;
		}
		return null;
	}

	private static void addLinkListener(final String author, final Link link) {
		link.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				launch();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				launch();
			}

			private void launch() {
				Program.launch(DrcUiActivator.PROFILE_ROOT + author);
			}
		});
	}
}
