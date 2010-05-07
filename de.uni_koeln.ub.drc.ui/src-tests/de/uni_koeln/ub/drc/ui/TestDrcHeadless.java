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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.contexts.ContextManager;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.internal.services.EclipseAdapter;
import org.eclipse.e4.core.services.Adapter;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.spi.ContextInjectionFactory;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.core.services.context.spi.ISchedulerStrategy;
import org.eclipse.e4.ui.internal.services.ActiveContextsFunction;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.swt.Activator;
import org.eclipse.e4.workbench.ui.IExceptionHandler;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.internal.ActivePartLookupFunction;
import org.eclipse.e4.workbench.ui.internal.ExceptionHandler;
import org.eclipse.e4.workbench.ui.internal.ReflectionContributionFactory;
import org.eclipse.e4.workbench.ui.internal.UIEventPublisher;
import org.eclipse.e4.workbench.ui.internal.WorkbenchLogger;
import org.eclipse.emf.common.notify.Notifier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Headless tests.
 * @author Fabian Steeg (fsteeg)
 */
// @RunWith(SWTBotJunit4ClassRunner.class) //TODO TestableObject violates loader constraints
public class TestDrcHeadless {

  protected IEclipseContext osgiContext;
  protected IEclipseContext applicationContext;
  protected MApplication application;
  protected IPresentationEngine renderer;
  protected MApplicationElement applicationElement;

  @Before public void setUp() throws Exception {
    Assert.assertTrue("Tests should run as JUnit Plug-in Tests", Platform.isRunning());
    osgiContext = EclipseContextFactory.getServiceContext(Activator.getDefault().getBundle()
        .getBundleContext());
    applicationContext = createApplicationContext(osgiContext);
    applicationContext.set(IContextConstants.DEBUG_STRING, "Application Context"); //$NON-NLS-1$
    applicationElement = HeadlessSetup.createApplicationElement(applicationContext, getUri());
    renderer = createPresentationEngine(getEngineURI(), applicationContext);
    // Hook the global notifications
    ((Notifier) applicationElement).eAdapters().add(new UIEventPublisher(applicationContext));
    application = (MApplication) applicationElement;
    for (MWindow wbw : application.getChildren()) {
      createGUI(wbw);
    }
  }

  @After public void tearDown() throws Exception {
    if (applicationContext instanceof IDisposable) {
      ((IDisposable) applicationContext).dispose();
    }
  }

  /* Tests to be overridden in the UI tests: */

  @Test public void switchActivePart1InContext() throws Exception {
    IEclipseContext context = application.getContext();
    MPart[] parts = getThreeParts();
    switchTo(context, parts[0]);
  }

  @Test public void switchActivePart2InContext() throws Exception {
    IEclipseContext context = application.getContext();
    MPart[] parts = getThreeParts();
    switchTo(context, parts[1]);
  }

  @Test public void switchActivePart3InContext() throws Exception {
    IEclipseContext context = application.getContext();
    MPart[] parts = getThreeParts();
    switchTo(context, parts[2]);
  }

  /* Tests unspecific to UI, should always pass: */

  @Test public final void getActiveContexts() throws Exception {
    assertNotNull(application.getContext().get(IServiceConstants.ACTIVE_CONTEXTS));
  }

  @Test public final void getSelection() throws Exception {
    // Selection is null in headless mode - is this correct?
    assertNull(application.getContext().get(IServiceConstants.SELECTION));
  }

  @Test public final void getActivePart() throws Exception {
    assertNull(application.getContext().get(IServiceConstants.ACTIVE_PART));
  }

  @Test public final void getInput() throws Exception {
    assertNull(application.getContext().get(IServiceConstants.INPUT));
  }

  @Test public final void getPersistedState() throws Exception {
    assertNull(application.getContext().get(IServiceConstants.PERSISTED_STATE));
  }

  @Test public final void getActivePartId() throws Exception {
    assertNull(application.getContext().get(IServiceConstants.ACTIVE_PART_ID));
  }

  protected String getUri() {
    return AllTestsSuite.APPLICATION_XMI;
  }

  protected MPart[] getParts() {
    return find(AllTestsSuite.PART_NAMES);
  }

  protected ISchedulerStrategy getApplicationSchedulerStrategy() {
    return null;
  }

  protected IEclipseContext createApplicationContext(IEclipseContext osgiContext) {
    assertNotNull(osgiContext);
    final IEclipseContext appContext = EclipseContextFactory.create(osgiContext,
        getApplicationSchedulerStrategy());
    appContext.set(IEclipseContext.class.getName(), appContext);
    appContext.set(IContributionFactory.class.getName(), new ReflectionContributionFactory(
        (IExtensionRegistry) appContext.get(IExtensionRegistry.class.getName())));
    appContext.set(IExceptionHandler.class.getName(), new ExceptionHandler());
    appContext.set(Logger.class.getName(), new WorkbenchLogger());
    appContext.set(Adapter.class.getName(), ContextInjectionFactory.inject(new EclipseAdapter(),
        appContext));
    appContext.set(ContextManager.class.getName(), new ContextManager());
    appContext.set(IServiceConstants.ACTIVE_CONTEXTS, new ActiveContextsFunction());
    appContext.set(IServiceConstants.ACTIVE_PART, new ActivePartLookupFunction());
    /* For testing part switching: */
    appContext.runAndTrack(HeadlessSetup.RUNNABLE, null);
    return appContext;
  }

  protected IPresentationEngine createPresentationEngine(String renderingEngineURI,
      IEclipseContext applicationContext) throws Exception {
    IContributionFactory contributionFactory = (IContributionFactory) applicationContext
        .get(IContributionFactory.class.getName());
    Object newEngine = contributionFactory.create(renderingEngineURI, applicationContext);
    return (IPresentationEngine) newEngine;
  }

  private void switchTo(IEclipseContext context, MPart part) {
    context.set(IServiceConstants.ACTIVE_PART, part);
    assertEquals(part.getId(), context.get(IServiceConstants.ACTIVE_PART_ID));
    // the OSGi context should not have been affected:
    assertNull(osgiContext.get(IServiceConstants.ACTIVE_PART));
    assertNull(osgiContext.get(IServiceConstants.ACTIVE_PART_ID));
  }

  protected MPart[] getThreeParts() {
    MPart firstPart = getParts()[0];
    assertNotNull(firstPart);
    MPart secondPart = getParts()[1];
    assertNotNull(secondPart);
    assertFalse(firstPart.equals(secondPart));
    MPart thirdPart = getParts()[2];
    assertNotNull(thirdPart);
    assertFalse(secondPart.equals(thirdPart));
    return new MPart[] { firstPart, secondPart, thirdPart };
  }

  protected void createGUI(MUIElement uiRoot) {
    try {
      renderer.createGui(uiRoot);
    } catch (Exception x) {
      x.printStackTrace();
      fail("Could not create GUI: " + x.getMessage());
    }
  }

  protected String getEngineURI() {
    return HeadlessSetup.ENGINE_URI;
  }

  protected MPart[] find(String... names) {
    List<MPart> result = new ArrayList<MPart>();
    for (String name : names) {
      result.add((MPart) HeadlessSetup.findElement(application, name));
    }
    return result.toArray(new MPart[] {});
  }

}
