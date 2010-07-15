/**************************************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: IBM Corporation - initial API and implementation; Fabian Steeg - adopted for DRC
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IContextConstants;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.internal.workbench.swt.ResourceUtility;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.IResourceUtilities;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.w3c.dom.css.CSSStyleDeclaration;

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
    bundleContext = DrcUiActivator.instance().getBundle().getBundleContext();
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
    Assert.assertEquals(DrcUiActivator.PLUGIN_ID, bundleContext.getBundle().getSymbolicName());
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
    Assert.assertNull(application.getContext().get(IServiceConstants.ACTIVE_SHELL));
  }

  @Test public void getActiveChild() throws Exception {
    Assert.assertNotNull("Active child of application context should not be null", application
        .getContext().get(IContextConstants.ACTIVE_CHILD));
  }

  @Test public void getActiveContextsUi() throws Exception {
    Assert.assertNotNull(getActiveChildContext(application).get(IServiceConstants.ACTIVE_CONTEXTS));
  }

  @Test public void getSelectionUi() throws Exception {
    Assert.assertNotNull(getActiveChildContext(application).get(IServiceConstants.ACTIVE_SELECTION));
  }

  @Test public void getActiveChildUi() throws Exception {
    Assert.assertNotNull(getActiveChildContext(application).get(IContextConstants.ACTIVE_CHILD));
  }

  @Test public void getActiveShellUi() throws Exception {
    Assert.assertNull(getActiveChildContext(application).get(IServiceConstants.ACTIVE_SHELL));
  }

  @Test public void getPersistedStateUi() throws Exception {
    Assert.assertNull(getActiveChildContext(application).get(IServiceConstants.PERSISTED_STATE));
  }

  @Test @Override public void switchActivePart1InContext() throws Exception {
    final IEclipseContext context = application.getContext();
    final MPart[] parts = getFourParts();
    Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
      public void run() {
        switchTo(context, parts[1]);
      }
    });
  }

  @Test @Override public void switchActivePart2InContext() throws Exception {
    final IEclipseContext context = application.getContext();
    final MPart[] parts = getFourParts();
    Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
      public void run() {
        switchTo(context, parts[1]);
      }
    });
  }

  @Test @Override public void switchActivePart3InContext() throws Exception {
    final IEclipseContext context = application.getContext();
    final MPart[] parts = getFourParts();
    Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
      public void run() {
        switchTo(context, parts[2]);
      }
    });
  }

  private void switchTo(final IEclipseContext context, final MPart part) {
    context.set(IServiceConstants.ACTIVE_PART, part);
    while (display.readAndDispatch()) {};
    Assert.assertEquals(part.getElementId(), context.get(IServiceConstants.ACTIVE_PART_ID));
  }

  @Override protected IEclipseContext createApplicationContext(final IEclipseContext osgiContext) {
    final IEclipseContext[] contexts = new IEclipseContext[1];
    Realm.runWithDefault(SWTObservables.getRealm(display), new Runnable() {
      public void run() {
        contexts[0] = TestDrcUi.super.createApplicationContext(osgiContext);
        contexts[0]
            .set(IResourceUtilities.class.getName(), new ResourceUtility(/*getPackageAdmin()*/));
        contexts[0].set(IStylingEngine.class.getName(), new IStylingEngine() {
          public void style(final Object widget) {}

          public void setId(final Object widget, final String id) {}

          public void setClassname(final Object widget, final String classname) {}

        @Override public CSSStyleDeclaration getStyle(Object widget) {
            return null;
        }
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
          Assert.fail("Could not create GUI: " + x.getMessage());
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
        Assert.assertNotNull("Part context should not be null", part.getContext());
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
      Assert.assertEquals(newValue, context.get(variable));
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

  private static MWindowElement getNonContainer(MWindowElement activeChild) {
    if (activeChild instanceof MElementContainer<?>) {
      activeChild = (MWindowElement) ((MElementContainer<?>) activeChild).getSelectedElement();
      Assert.assertNotNull(activeChild);
      activeChild = getNonContainer(activeChild);
    }
    return activeChild;
  }

  private static IEclipseContext getActiveChildContext(final MApplication application) {
    MWindowElement nonContainer = getNonContainer(application.getSelectedElement()
        .getSelectedElement());
    return ((MContext) nonContainer).getContext();
  }
}
