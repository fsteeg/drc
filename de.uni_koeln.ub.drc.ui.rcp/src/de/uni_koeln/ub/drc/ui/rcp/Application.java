/**************************************************************************************************
 * Copyright (c) 2011 Mihail Atanassov. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Mihail Atanassov - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.rcp;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;

import de.uni_koeln.ub.drc.ui.DrcUiActivator;

/**
 * This class controls all aspects of the application's execution
 * 
 * @author Mihail Atanassov (matana), Fabian Steeg (fsteeg)
 */
public class Application implements IApplication {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.
	 * IApplicationContext)
	 */
	@Override
	public Object start(IApplicationContext context) throws Exception {
		update(DrcUiActivator.getDefault().getBundleContext());
		Display display = PlatformUI.createDisplay();
		try {
			int returnCode = PlatformUI.createAndRunWorkbench(display,
					new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART)
				return IApplication.EXIT_RESTART;
			return IApplication.EXIT_OK;
		} finally {
			display.dispose();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	@Override
	public void stop() {
		if (!PlatformUI.isWorkbenchRunning())
			return;
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}

	private void update(BundleContext context) throws URISyntaxException {
		//URI repo = new URI("http://hydra2.spinfo.uni-koeln.de/p2"); //$NON-NLS-1$
		URI repo = new URI("http://bob.spinfo.uni-koeln.de/p2"); //$NON-NLS-1$
		//URI repo = new URI("file:///Users/fsteeg/Documents/workspaces/drc/de.uni_koeln.ub.drc.rcp/target/repository"); //$NON-NLS-1$
		String productId = "de.uni_koeln.ub.drc.rcp"; //$NON-NLS-1$
		InstallOperation op = createInstallOperation(context, repo, productId);
		if (op != null) {
			IStatus status = null;
			try {
				status = op.resolveModal(null);
			} catch (IllegalArgumentException x) {
				DrcUiActivator
						.getDefault()
						.getLog()
						.log(new Status(IStatus.ERROR,
								DrcUiActivator.PLUGIN_ID, x.getMessage()));
				return;
			}
			DrcUiActivator
					.getDefault()
					.getLog()
					.log(new Status(
							IStatus.INFO,
							DrcUiActivator.PLUGIN_ID,
							String.format(
									"Resolved operation status: %s, details: %s", status, op.getResolutionDetails()))); //$NON-NLS-1$
			ProvisioningJob job = op.getProvisioningJob(null);
			if (job != null) {
				job.addJobChangeListener(new JobChangeAdapter() {
					@Override
					public void done(IJobChangeEvent event) {
						DrcUiActivator
								.getDefault()
								.getLog()
								.log(new Status(IStatus.INFO,
										DrcUiActivator.PLUGIN_ID,
										"Update Done: " + event.getResult())); //$NON-NLS-1$
					}
				});
				job.schedule();
			}
		}
	}

	private InstallOperation createInstallOperation(BundleContext context,
			URI repo, String productId) {
		try {
			final IProvisioningAgent agent = (IProvisioningAgent) context
					.getService(context
							.getServiceReference(IProvisioningAgent.SERVICE_NAME));
			IMetadataRepository metadataRepo = ((IMetadataRepositoryManager) agent
					.getService(IMetadataRepositoryManager.SERVICE_NAME))
					.loadRepository(repo, null);
			((IArtifactRepositoryManager) agent
					.getService(IArtifactRepositoryManager.SERVICE_NAME))
					.loadRepository(repo, null);
			Set<IInstallableUnit> toInstall = metadataRepo.query(
					QueryUtil.createIUQuery(productId), null)
					.toUnmodifiableSet();
			DrcUiActivator
					.getDefault()
					.getLog()
					.log(new Status(IStatus.INFO, DrcUiActivator.PLUGIN_ID,
							"Attempting to install: " + toInstall)); //$NON-NLS-1$
			return new InstallOperation(new ProvisioningSession(agent),
					toInstall);
		} catch (ProvisionException e) {
			e.printStackTrace();
		} catch (NullPointerException e) { // failing update for inner workbench
			e.printStackTrace();
		}
		return null;
	}
}
