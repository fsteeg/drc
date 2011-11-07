/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/

package de.uni_koeln.ub.drc.ui.login;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.uni_koeln.ub.drc.ui.Messages;

/**
 * Handles the callbacks from the LoginModule with a login dialog.
 * 
 * @author Fabian Steeg (fsteeg), Mihail Atanassov (matana)
 */
public class SimpleLoginDialog extends /* TitleArea */Dialog implements
		CallbackHandler {

	/**
	 * The class / CallbackHandler ID
	 */
	public static final String ID = SimpleLoginDialog.class.getName()
			.toLowerCase();
	private static final String TITLE = Messages.get().LoginToDrc;
	private static final Point SIZE = new Point(300, 175);
	private List<Callback> callbacks;

	/**
	 * Creates a login dialog on the default disaply.
	 */
	public SimpleLoginDialog() {
		super(Display.getDefault().getActiveShell());
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX
				| SWT.APPLICATION_MODAL);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
	 */
	@Override
	public void handle(final Callback[] callbacks) throws IOException {
		this.callbacks = Arrays.asList(callbacks);
		openDialog();
	}

	private void openDialog() {
		setBlockOnOpen(true);
		open();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	@Override
	protected void cancelPressed() {
		this.close();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		shell.setText(TITLE);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
	 */
	@Override
	protected Point getInitialSize() {
		return SIZE;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(final Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);
		dialogArea.setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite composite = new Composite(dialogArea, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		createCallbackHandlers(composite);
		return composite;
	}

	private void createCallbackHandlers(final Composite composite) {
		for (Callback callback : callbacks) {
			if (callback instanceof NameCallback) {
				createNameHandler(composite, (NameCallback) callback);
			} else if (callback instanceof PasswordCallback) {
				createPasswordHandler(composite, (PasswordCallback) callback);
			}
		}
	}

	private void createPasswordHandler(final Composite composite,
			final PasswordCallback callback) {
		Label label = new Label(composite, SWT.NONE);
		label.setText(callback.getPrompt());
		final Text text = new Text(composite, SWT.SINGLE | SWT.LEAD
				| SWT.PASSWORD | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent event) {
				callback.setPassword(text.getText().toCharArray());
			}
		});
	}

	private void createNameHandler(final Composite composite,
			final NameCallback callback) {
		Label label = new Label(composite, SWT.NONE);
		label.setText(callback.getPrompt());
		final Text text = new Text(composite, SWT.SINGLE | SWT.LEAD
				| SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent event) {
				callback.setName(text.getText());
			}
		});
		text.setFocus();
	}
}
