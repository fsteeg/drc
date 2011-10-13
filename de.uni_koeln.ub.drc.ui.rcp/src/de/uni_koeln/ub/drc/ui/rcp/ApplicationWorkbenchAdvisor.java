package de.uni_koeln.ub.drc.ui.rcp;

import java.net.URL;

import javax.security.auth.login.LoginException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.equinox.security.auth.ILoginContext;
import org.eclipse.equinox.security.auth.LoginContextFactory;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import de.uni_koeln.ub.drc.ui.ApplicationWorkbenchWindowAdvisor;
import de.uni_koeln.ub.drc.ui.DrcUiActivator;
import de.uni_koeln.ub.drc.ui.Messages;

/**
 * This workbench advisor creates the window advisor, and specifies the
 * perspective id for the initial window.
 */
@SuppressWarnings("restriction")
public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private static final String JAAS_CONFIG_FILE = "jaas_config"; //$NON-NLS-1$
	private ILoginContext loginContext;
	private static final String PERSPECTIVE_ID = "de.uni_koeln.ub.drc.ui.perspective";

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void initialize(IWorkbenchConfigurer configurer) {
		super.initialize(configurer);
		Bundle b = FrameworkUtil.getBundle(getClass());
		BundleContext context = b.getBundleContext();
		ServiceReference serviceRef = context
				.getServiceReference(IThemeManager.class.getName());
		IThemeManager themeManager = (IThemeManager) context
				.getService(serviceRef);

		final IThemeEngine engine = themeManager.getEngineForDisplay(Display
				.getCurrent());
		engine.setTheme("de.uni_koeln.ub.drc.ui.rcp.theme", true); //$NON-NLS-1$
		if (serviceRef != null) {
			serviceRef = null;
		}
		if (themeManager != null) {
			themeManager = null;
		}
	}

	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}

	@Override
	public void postStartup() {
		super.postStartup();
		BundleContext bundleContext = DrcUiActivator.getDefault().getBundle()
				.getBundleContext();
		try {
			login(bundleContext);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void login(BundleContext bundleContext) throws Exception {
		String configName = "SIMPLE"; //$NON-NLS-1$
		System.out.println("bundleContext : "
				+ bundleContext.getClass().getName().toLowerCase());
		URL configUrl = bundleContext.getBundle().getEntry(JAAS_CONFIG_FILE);
		loginContext = LoginContextFactory.createContext(configName, configUrl);
		try {
			loginContext.login();
			DrcUiActivator.getDefault().setLoginContext(loginContext);
		} catch (LoginException e) {
			e.printStackTrace();
			IStatus status = new Status(IStatus.ERROR,
					"de.uni_koeln.ub.drc.ui", "Login failed", e); //$NON-NLS-1$ //$NON-NLS-2$
			int result = ErrorDialog.openError(null, Messages.get().Error,
					Messages.get().LoginFailed, status);
			if (result == ErrorDialog.CANCEL) {
				stop(bundleContext);
				System.exit(0);
			} else {
				login(bundleContext);
			}
		}
	}

	private void stop(BundleContext bundleContext) {
		try {
			DrcUiActivator.getDefault().stop(bundleContext);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
