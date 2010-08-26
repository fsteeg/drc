/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/

package de.uni_koeln.ub.drc.ui.login;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.eclipse.swt.SWT;

import de.uni_koeln.ub.drc.data.User;
import de.uni_koeln.ub.drc.ui.DrcUiActivator;
import de.uni_koeln.ub.drc.ui.views.EditComposite;

/**
 * Simple login module implementation using credentials from a properties file.
 * @author Fabian Steeg (fsteeg)
 */
public final class SimpleLoginModule implements LoginModule {

  private static final String ACCOUNTS = "accounts.properties";
  private CallbackHandler callbackHandler;
  private boolean loggedIn;
  private Subject subject;
  private User currentUser;
  private Properties users;

  /**
   * {@inheritDoc}
   * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject,
   *      javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
   */
  @SuppressWarnings( "rawtypes" )
  /* from implemented API */
  @Override
  public void initialize(final Subject subject, final CallbackHandler callbackHandler,
      final Map sharedState, final Map options) {
    this.subject = subject;
    this.callbackHandler = callbackHandler;
    URL accountPropertiesUrl = DrcUiActivator.instance().getBundle().getEntry(ACCOUNTS);
    users = new Properties();
    try {
      users.load(accountPropertiesUrl.openStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * {@inheritDoc}
   * @see javax.security.auth.spi.LoginModule#login()
   */
  @Override
  public boolean login() throws LoginException {
    NameCallback userCallback = new NameCallback("User:");
    PasswordCallback passCallback = new PasswordCallback("Password:", false);
    try {
      callbackHandler.handle(new Callback[] {
      /* new TextOutputCallback(TextOutputCallback.INFORMATION, "Please login"), */userCallback,
          passCallback });
    } catch (IOException e) {
      e.printStackTrace();
    } catch (UnsupportedCallbackException e) {
      e.printStackTrace();
    }
    return authenticate(userCallback, passCallback);
  }

  private boolean authenticate(final NameCallback userCallback, final PasswordCallback passCallback) {
    String name = userCallback.getName();
    String pass = passCallback.getPassword() != null ? new String(passCallback.getPassword()) : "";
    if (validLogin(name, pass)) {
      loggedIn = true;
      try {
        String folder = DrcUiActivator.instance().usersFolder();
        currentUser = User.withId(name, folder);
      } catch (Throwable x) {
        x.printStackTrace();
      }
      System.out.println("Logged in: " + currentUser);
    }
    return loggedIn;
  }

  private boolean validLogin(final String name, final String pass) {
    return users.getProperty(name).trim().equals(pass);
  }

  /**
   * {@inheritDoc}
   * @see javax.security.auth.spi.LoginModule#commit()
   */
  @Override
  public boolean commit() throws LoginException {
    subject.getPublicCredentials()
        .add(
            String.format("%s from %s (%s)", currentUser.name(), currentUser.region(),
                currentUser.id()));
    subject.getPrivateCredentials().add(currentUser);
    return loggedIn;
  }

  /**
   * {@inheritDoc}
   * @see javax.security.auth.spi.LoginModule#abort()
   */
  @Override
  public boolean abort() throws LoginException {
    loggedIn = false;
    return true;
  }

  /**
   * {@inheritDoc}
   * @see javax.security.auth.spi.LoginModule#logout()
   */
  @Override
  public boolean logout() throws LoginException {
    loggedIn = false;
    return true;
  }
}
