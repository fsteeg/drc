/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui;

import java.net.URL;
import java.util.Collections;

import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.auth.ILoginContext;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.BundleContext;

import com.quui.sinist.XmlDb;

import de.uni_koeln.ub.drc.data.Index;
import de.uni_koeln.ub.drc.data.User;
import de.uni_koeln.ub.drc.ui.views.SearchView;

/**
 * The activator class controls the plug-in life cycle
 */
/**
 * @author Fabian Steeg (fsteeg), Mihail Atanassov (matana)
 * 
 */
public class DrcUiActivator extends Plugin {

	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "de.uni_koeln.ub.drc.ui"; //$NON-NLS-1$
	// The shared instance
	private static DrcUiActivator plugin;
	private XmlDb db;
	private ILoginContext loginContext;
	private SearchView searchView;
	private BundleContext context;

	/**
	 * The user ID of the OCR
	 */
	public static final String OCR_ID = "OCR"; //$NON-NLS-1$

	/**
	 * The portal page root address for a user
	 */
	public static final String PROFILE_ROOT = "http://hydra1.spinfo.uni-koeln.de:9000/application/user?id="; //$NON-NLS-1$

	/**
	 * The constructor
	 */
	public DrcUiActivator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		this.context = context;
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static DrcUiActivator getDefault() {
		return plugin;
	}

	/**
	 * @return The data DB we are using
	 */
	public XmlDb db() {
		if (db == null) {
			db = Index.LocalDb().isAvailable() ? Index.LocalDb()
					: currentUser().db();
			if (!db.isAvailable()) {
				throw new IllegalStateException(
						"Could not connect to DB: " + db); //$NON-NLS-1$
			}
			getLog().log(new Status(IStatus.INFO, PLUGIN_ID, "Using DB: " + db)); //$NON-NLS-1$
		}
		return db;
	}

	/**
	 * @return The users DB we are using
	 */
	public XmlDb userDb() {
		return Index.LocalDb().isAvailable() ? Index.LocalDb() : new XmlDb(
				"hydra1.spinfo.uni-koeln.de", 8080, "guest", "guest"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * @return The user that is currently logged in
	 */
	public User currentUser() {
		User user = null;
		try {
			ILoginContext context = getLoginContext();
			user = (User) context.getSubject().getPrivateCredentials()
					.iterator().next();
		} catch (LoginException e) {
			e.printStackTrace();
		}
		return user;
	}

	/**
	 * @return The context for the logged in user.
	 */
	public ILoginContext getLoginContext() {
		return loginContext;
	}

	/**
	 * @param location
	 *            The bundle path of the image to load
	 * @return The loaded image
	 */
	public Image loadImage(String location) {
		IPath path = new Path(location);
		URL url = FileLocator.find(this.getBundle(), path,
				Collections.EMPTY_MAP);
		ImageDescriptor desc = ImageDescriptor.createFromURL(url);
		return desc.createImage();
	}

	/**
	 * @param loginContext
	 *            The ILoginContext
	 */
	public void setLoginContext(ILoginContext loginContext) {
		this.loginContext = loginContext;
		this.searchView.setInput();
		this.searchView.select();
	}

	/**
	 * @param searchView
	 *            The SearchView
	 */
	public void register(SearchView searchView) {
		this.searchView = searchView;
	}

	/**
	 * @return The BundleContext
	 */
	public BundleContext getBundleContext() {
		return context;
	}

}
