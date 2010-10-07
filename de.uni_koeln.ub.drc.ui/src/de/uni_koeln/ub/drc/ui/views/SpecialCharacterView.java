/**************************************************************************************************
 * Copyright (c) 2010 Mihail Atanassov. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.views;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.css.swt.CSSSWTConstants;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * View containing buttons for insertion of special characters.
 * @author Mihail Atanassov (matana)
 */

public final class SpecialCharacterView {

  private static final char[] SC = new char[] { /* A */'\u00C0', '\u00E0', '\u00C1', '\u00E1',
      '\u00C2', '\u00E2', '\u00C3', '\u00E3', '\u00C4', '\u00E4', '\u00C6', '\u00E6', '\u1EA0',
      '\u1EA1', /* E */'\u00C8', '\u00E8', '\u00C9', '\u00E9', '\u00CA', '\u00EA', '\u00CB',
      '\u00EB', '\u1EBC', '\u1EBD', '\u0113', /* I */'\u00CC', '\u00EC', '\u00CD', '\u00ED',
      '\u00CE', '\u00EE', '\u00CF', '\u00EF', '\u0128', '\u0129', '\u012A', '\u012B', '\u0130', /* O */
      '\u00D2', '\u00F2', '\u00D3', '\u00F3', '\u00D4', '\u00F4', '\u00D5', '\u00F5', '\u00D6',
      '\u00F6', '\u014C', '\u014D', '\u0152', '\u0153', /* U */'\u00D9', '\u00F9', '\u00DA',
      '\u00FA', '\u00DB', '\u00FB', '\u00DC', '\u00FC', '\u0168', '\u0169', '\u016A', '\u016B', /* N */
      '\u00D1', '\u00F1', /* long S */'\u017F' };

  @Inject
  private IEclipseContext context;
  private Text text;

  @Inject
  public SpecialCharacterView(final Composite parent) {
    Composite specialCharacterComposite = new Composite(parent, SWT.NONE);
    RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
    rowLayout.spacing = 5;
    rowLayout.pack = true;
    specialCharacterComposite.setLayout(rowLayout);
    initSCButtons(specialCharacterComposite);
  }

  @Inject
  public void setSelection(@Optional @Named( IServiceConstants.ACTIVE_SELECTION ) final Text text) {
    if (text != null) {
      this.text = text;
    }
  }

  private void initSCButtons(final Composite specialCharacterComposite) {
    for (int i = 0; i < SC.length; i++) {
      final Button button = new Button(specialCharacterComposite, SWT.PUSH | SWT.FLAT);
      RowData rowData = new RowData(30, 30);
      button.setLayoutData(rowData);
      button.setData(CSSSWTConstants.CSS_CLASS_NAME_KEY, "specialCharacter");
      button.setText("" + SC[i]);
      button.addSelectionListener(new SelectionListener() {

        @Override
        public void widgetSelected(SelectionEvent e) {
          if (text != null) {
            int pos = text.getCaretPosition();
            text.insert(button.getText());
            text.setSelection(pos + 1);
          }
        }

        @Override
        public void widgetDefaultSelected(SelectionEvent e) {}
      });
    }
  }

}
