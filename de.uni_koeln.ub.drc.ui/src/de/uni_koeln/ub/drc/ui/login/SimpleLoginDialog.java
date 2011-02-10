/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/

package de.uni_koeln.ub.drc.ui.login;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.uni_koeln.ub.drc.ui.DrcUiActivator;
import de.uni_koeln.ub.drc.ui.Messages;

/**
 * Handles the callbacks from the LoginModule with a login dialog.
 * @author Fabian Steeg (fsteeg)
 */
public class SimpleLoginDialog extends /* TitleArea */Dialog implements CallbackHandler {

  private static final String TITLE = Messages.LoginToDrc;
  private static final String LOGIN = Messages.Login;
  private static final Point SIZE = new Point(250, 150);
  private boolean inputProcessed = false;
  private List<Callback> callbacks;

  /**
   * Creates a login dialog on the default disaply.
   */
  public SimpleLoginDialog() {
    super(Display.getDefault().getActiveShell());
  }

  /**
   * {@inheritDoc}
   * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
   */
  @Override
  public void handle(final Callback[] callbacks) throws IOException {
    this.callbacks = Arrays.asList(callbacks);
    openDialog();
    ModalContext.setAllowReadAndDispatch(true);
    try {
      ModalContext.run(waitForButtonPress(), true, new NullProgressMonitor(), Display.getDefault());
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void openDialog() {
    Display.getDefault().syncExec(new Runnable() {
      public void run() {
        setBlockOnOpen(false);
        open();
        hookIntoOkButton();
      }
    });
  }

  private IRunnableWithProgress waitForButtonPress() {
    final int interval = 100;
    return new IRunnableWithProgress() {
      public void run(final IProgressMonitor monitor) {
        while (!inputProcessed) {
          try {
            Thread.sleep(interval);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        inputProcessed = false;
      }
    };
  }

  private void hookIntoOkButton() {
    final Button okButton = getButton(IDialogConstants.OK_ID);
    okButton.setText(LOGIN);
    okButton.addSelectionListener(new SelectionListener() {
      public void widgetSelected(final SelectionEvent event) {
        inputProcessed = true;
      }

      public void widgetDefaultSelected(final SelectionEvent event) {}
    });
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
   */
  @Override
  protected void cancelPressed() {
    /* If the login is cancelled, we shut down: */
    DrcUiActivator activator = DrcUiActivator.instance();
    try {
      activator.stop(activator.getBundle().getBundleContext());
    } catch (Exception e) {
      e.printStackTrace();
    }
    System.exit(0); // OK here?
  };

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell(final Shell shell) {
    super.configureShell(shell);
    shell.setText(TITLE);
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
   */
  protected Point getInitialSize() {
    return SIZE;
  }

  /**
   * {@inheritDoc}
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
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
  
  private void createPasswordHandler(final Composite composite, final PasswordCallback callback) {
    Label label = new Label(composite, SWT.NONE);
    label.setText(callback.getPrompt());
    final Text text = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.PASSWORD | SWT.BORDER);
    text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    text.addModifyListener(new ModifyListener() {
      public void modifyText(final ModifyEvent event) {
        callback.setPassword(text.getText().toCharArray());
      }
    });
  }

  private void createNameHandler(final Composite composite, final NameCallback callback) {
    Label label = new Label(composite, SWT.NONE);
    label.setText(callback.getPrompt());
    final Text text = new Text(composite, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
    text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    text.addModifyListener(new ModifyListener() {
      public void modifyText(final ModifyEvent event) {
        callback.setName(text.getText());
      }
    });
  }
}
