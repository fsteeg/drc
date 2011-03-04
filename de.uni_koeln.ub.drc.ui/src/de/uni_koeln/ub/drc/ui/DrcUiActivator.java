/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.equinox.security.auth.ILoginContext;
import org.eclipse.equinox.security.auth.LoginContextFactory;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.BundleContext;

import com.quui.sinist.XmlDb;

import de.uni_koeln.ub.drc.data.Index;
import de.uni_koeln.ub.drc.data.User;

/**
 * Bundle activator.
 * @author Fabian Steeg (fsteeg)
 */
public final class DrcUiActivator extends Plugin {

  public static final String PLUGIN_ID = "de.uni_koeln.ub.drc.ui"; //$NON-NLS-1$

  private static final String JAAS_CONFIG_FILE = "jaas_config"; //$NON-NLS-1$

  public static final String PROFILE_ROOT = "http://hydra1.spinfo.uni-koeln.de:9000/application/user?id="; //$NON-NLS-1$

  public static final String OCR_ID = "OCR"; //$NON-NLS-1$

  private XmlDb db = null;

  private static DrcUiActivator instance;
  private ILoginContext loginContext;

  @Override
  public void start(final BundleContext context) throws Exception {
    super.start(context);
    instance = this;
    update(context);
    login(context);
  }

  private void update(BundleContext context) throws URISyntaxException {
    URI repo = new URI("http://hydra2.spinfo.uni-koeln.de/p2"); //$NON-NLS-1$
    //URI repo = new URI("http://hydra1.spinfo.uni-koeln.de/p2"); //$NON-NLS-1$
    //URI repo = new URI("file:///Users/fsteeg/Documents/workspaces/drc/de.uni_koeln.ub.drc.rcp/target/repository"); //$NON-NLS-1$
    String productId = "de.uni_koeln.ub.drc.rcp"; //$NON-NLS-1$
    InstallOperation op = createInstallOperation(context, repo, productId);
    if (op != null) {
      IStatus status = null;
      try {
        status = op.resolveModal(null);
      } catch (IllegalArgumentException x) {
        getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, x.getMessage()));
        return;
      }
      getLog().log(
          new Status(IStatus.INFO, PLUGIN_ID, String.format(
              "Resolved operation status: %s, details: %s", status, op.getResolutionDetails()))); //$NON-NLS-1$
      ProvisioningJob job = op.getProvisioningJob(null);
      if (job != null) {
        job.addJobChangeListener(new JobChangeAdapter() {
          public void done(IJobChangeEvent event) {
            getLog().log(new Status(IStatus.INFO, PLUGIN_ID, "Update Done: " + event.getResult())); //$NON-NLS-1$
          }
        });
        job.schedule();
      }
    }
  }

  private InstallOperation createInstallOperation(BundleContext context, URI repo, String productId) {
    try {
      final IProvisioningAgent agent = (IProvisioningAgent) context.getService(context
          .getServiceReference(IProvisioningAgent.SERVICE_NAME));
      IMetadataRepository metadataRepo = ((IMetadataRepositoryManager) agent
          .getService(IMetadataRepositoryManager.SERVICE_NAME)).loadRepository(repo, null);
      ((IArtifactRepositoryManager) agent.getService(IArtifactRepositoryManager.SERVICE_NAME))
          .loadRepository(repo, null);
      Set<IInstallableUnit> toInstall = metadataRepo
          .query(QueryUtil.createIUQuery(productId), null).toUnmodifiableSet();
      getLog().log(new Status(IStatus.INFO, PLUGIN_ID, "Attempting to install: " + toInstall)); //$NON-NLS-1$
      return new InstallOperation(new ProvisioningSession(agent), toInstall);
    } catch (ProvisionException e) {
      e.printStackTrace();
    }
    return null;
  }

  public User currentUser() {
    User user = null;
    try {
      user = (User) getLoginContext().getSubject().getPrivateCredentials().iterator().next();
    } catch (LoginException e) {
      e.printStackTrace();
    }
    return user;
  }

  private void login(final BundleContext context) throws Exception {
    String configName = "SIMPLE"; //$NON-NLS-1$
    URL configUrl = context.getBundle().getEntry(JAAS_CONFIG_FILE);
    loginContext = LoginContextFactory.createContext(configName, configUrl);
    try {
      loginContext.login();
    } catch (LoginException e) {
      IStatus status = new Status(IStatus.ERROR, "de.uni_koeln.ub.drc.ui", "Login failed", e); //$NON-NLS-1$ //$NON-NLS-2$
      int result = ErrorDialog.openError(null, Messages.Error, Messages.LoginFailed, status);
      if (result == ErrorDialog.CANCEL) {
        stop(context);
        System.exit(0);
      } else {
        login(context);
      }
    }
  }

  /**
   * @return The active bundle, or null
   */
  public static DrcUiActivator instance() {
    return instance;
  }

  /**
   * @return The context for the logged in user.
   */
  public ILoginContext getLoginContext() {
    return loginContext;
  }

  @Override
  public void stop(final BundleContext context) throws Exception {
    instance = null;
    super.stop(context);
  }

  /**
   * @param location The name of the file to retrieve, relative to the bundle root
   * @return The file at the given location
   */
  public File fileFromBundle(final String location) {
    try {
      URL resource = getBundle().getResource(location);
      if (resource == null) {
        getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, "Could not resolve: " + location)); //$NON-NLS-1$
        return null;
      }
      return new File(FileLocator.resolve(resource).toURI());
    } catch (URISyntaxException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public Image loadImage(String location) {
    IPath path = new Path(location);
    URL url = FileLocator.find(this.getBundle(), path, Collections.EMPTY_MAP);
    ImageDescriptor desc = ImageDescriptor.createFromURL(url);
    return desc.createImage();
  }

  public XmlDb db() {
    if (db == null) {
      db = Index.LocalDb().isAvailable() ? Index.LocalDb() : currentUser().db();
      if (!db.isAvailable()) {
        throw new IllegalStateException("Could not connect to DB: " + db); //$NON-NLS-1$
      }
      getLog().log(new Status(IStatus.INFO, PLUGIN_ID, "Using DB: " + db)); //$NON-NLS-1$
    }
    return db;
  }

  public XmlDb userDb() {
    return Index.LocalDb().isAvailable() ? Index.LocalDb() : new XmlDb(
        "xmldb:exist://hydra1.spinfo.uni-koeln.de:8080/exist/xmlrpc", "db", "drc"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

}
