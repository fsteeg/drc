/**************************************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: IBM Corporation - initial API and implementation; Fabian Steeg - adopted for DRC
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.core.services.context.spi.ISchedulerStrategy;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPSCElement;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.swt.internal.PartRenderingEngine;
import org.eclipse.e4.ui.workbench.swt.internal.ResourceUtility;
import org.eclipse.e4.workbench.ui.IResourceUtiltities;
import org.eclipse.e4.workbench.ui.internal.UISchedulerStrategy;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;

/**
 * UI tests.
 * @author Fabian Steeg (fsteeg)
 */
// @RunWith(SWTBotJunit4ClassRunner.class) //TODO TestableObject violates loader constraints
public final class TestDrcUi extends TestDrcHeadless {

  private BundleContext bundleContext;
  private ServiceTracker bundleTracker;
  private Display display;

  @Before @Override public void setUp() throws Exception {
    Assert.assertTrue("Tests should run as JUnit Plug-in Tests", Platform.isRunning());
    bundleContext = Activator.getDefault().getBundle().getBundleContext();
    display = Display.getDefault();
    super.setUp();
    while (display.readAndDispatch()) {};
  }

  @After @Override public void tearDown() throws Exception {
    super.tearDown();
    if (bundleTracker != null) {
      bundleTracker.close();
      bundleTracker = null;
    }
  }
  
  @Test public void bundleContext() {
    assertEquals(Activator.PLUGIN_ID, bundleContext.getBundle().getSymbolicName());
  }

  @Test public void firstPartGetContext() {
    testPart(getParts()[0]);
  }

  @Test public void secondPartGetContext() {
    testPart(getParts()[1]);
  }

  @Test public void thirdPartGetContext() {
    testPart(getParts()[2]);
  }

  @Test public void getActiveShell() throws Exception {
    assertNull(application.getContext().get(IServiceConstants.ACTIVE_SHELL));
  }

  @Test public void getActiveChild() throws Exception {
    assertNotNull("Active child of application context should not be null", application
        .getContext().get(IContextConstants.ACTIVE_CHILD));
  }

  @Test public void getActiveContextsUi() throws Exception {
    assertNotNull(getActiveChildContext(application).get(IServiceConstants.ACTIVE_CONTEXTS));
  }

  @Test public void getSelectionUi() throws Exception {
    assertNotNull(getActiveChildContext(application).get(IServiceConstants.SELECTION));
  }

  @Test public void getActiveChildUi() throws Exception {
    assertNotNull(getActiveChildContext(application).get(IContextConstants.ACTIVE_CHILD));
  }

  @Test public void getInputUi() throws Exception {
    assertNull(getActiveChildContext(application).get(IServiceConstants.INPUT));
  }

  @Test public void getActiveShellUi() throws Exception {
    assertNull(getActiveChildContext(application).get(IServiceConstants.ACTIVE_SHELL));
  }

  @Test public void getPersistedStateUi() throws Exception {
    assertNull(getActiveChildContext(application).get(IServiceConstants.PERSISTED_STATE));
  }

  @Test @Override public void switchActivePart1InContext() throws Exception {
    final IEclipseContext context = application.getContext();
    final MPart[] parts = getThreeParts();
    Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
      public void run() {
        switchTo(context, parts[1]);
      }
    });
  }

  @Test @Override public void switchActivePart2InContext() throws Exception {
    final IEclipseContext context = application.getContext();
    final MPart[] parts = getThreeParts();
    Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
      public void run() {
        switchTo(context, parts[1]);
      }
    });
  }

  @Test @Override public void switchActivePart3InContext() throws Exception {
    final IEclipseContext context = application.getContext();
    final MPart[] parts = getThreeParts();
    Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
      public void run() {
        switchTo(context, parts[2]);
      }
    });
  }

  private void switchTo(final IEclipseContext context, final MPart part) {
    context.set(IServiceConstants.ACTIVE_PART, part);
    while (display.readAndDispatch()) {};
    assertEquals(part.getId(), context.get(IServiceConstants.ACTIVE_PART_ID));
  }

  @Override protected ISchedulerStrategy getApplicationSchedulerStrategy() {
    return UISchedulerStrategy.getInstance();
  }

  @Override protected IEclipseContext createApplicationContext(final IEclipseContext osgiContext) {
    final IEclipseContext[] contexts = new IEclipseContext[1];
    Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
      public void run() {
        contexts[0] = TestDrcUi.super.createApplicationContext(osgiContext);
        contexts[0]
            .set(IResourceUtiltities.class.getName(), new ResourceUtility(getPackageAdmin()));
        contexts[0].set(IStylingEngine.class.getName(), new IStylingEngine() {
          public void style(final Object widget) {}

          public void setId(final Object widget, final String id) {}

          public void setClassname(final Object widget, final String classname) {}
        });
      }
    });
    return contexts[0];
  }

  @Override protected void createGUI(final MUIElement uiRoot) {
    Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
      public void run() {
        try {
          TestDrcUi.super.createGUI(uiRoot);
        } catch (Exception x) {
          x.printStackTrace();
          fail("Could not create GUI: " + x.getMessage());
        }
      }
    });
  }

  @Override protected String getEngineURI() {
    return PartRenderingEngine.engineURI;
  }

  private void testPart(final MPart part) {
    /*
     * wrap this since the renderer will try to build the UI for the part if it hasn't been built
     */
    Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
      public void run() {
        part.getParent().setSelectedElement(part);
        assertNotNull("Part context should not be null", part.getContext());
        testModify(part);
      }
    });
  }

  private PackageAdmin getPackageAdmin() {
    if (bundleTracker == null) {
      if (bundleContext == null) {
        return null;
      }
      bundleTracker = new ServiceTracker(bundleContext, PackageAdmin.class.getName(), null);
      bundleTracker.open();
    }
    return (PackageAdmin) bundleTracker.getService();
  }

  private void testModify(final MContext mcontext) {
    Set<String> variables = getVariables(mcontext, new HashSet<String>());
    IEclipseContext context = mcontext.getContext();
    for (String variable : variables) {
      Object newValue = new Object();
      context.modify(variable, newValue);
      assertEquals(newValue, context.get(variable));
    }
  }

  private static Set<String> getVariables(final MContext context, final Set<String> variables) {
    variables.addAll(context.getVariables());
    if (context instanceof MUIElement) {
      MElementContainer<?> parent = ((MUIElement) context).getParent();
      while (parent != null) {
        if (parent instanceof MContext) {
          getVariables((MContext) parent, variables);
        }
        parent = parent.getParent();
      }
    }
    return variables;
  }

  private static MPSCElement getNonContainer(MPSCElement activeChild) {
    if (activeChild instanceof MElementContainer<?>) {
      activeChild = (MPSCElement) ((MElementContainer<?>) activeChild).getSelectedElement();
      assertNotNull(activeChild);
      activeChild = getNonContainer(activeChild);
    }
    return activeChild;
  }

  private static IEclipseContext getActiveChildContext(final MApplication application) {
    MPSCElement nonContainer = getNonContainer(application.getSelectedElement()
        .getSelectedElement());
    return ((MContext) nonContainer).getContext();
  }
}
