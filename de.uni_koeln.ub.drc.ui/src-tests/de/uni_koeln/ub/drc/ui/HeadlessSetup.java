/**************************************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: IBM Corporation - initial API and implementation; Fabian Steeg - adopted for DRC
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.commands.Category;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.services.IContributionFactory;
import org.eclipse.e4.core.services.IDisposable;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.core.services.context.ContextChangeEvent;
import org.eclipse.e4.core.services.context.EclipseContextFactory;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.e4.core.services.context.IRunAndTrack;
import org.eclipse.e4.core.services.context.spi.IContextConstants;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MApplicationFactory;
import org.eclipse.e4.ui.model.application.MApplicationPackage;
import org.eclipse.e4.ui.model.application.MCommand;
import org.eclipse.e4.ui.model.application.MContext;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.model.application.MElementContainer;
import org.eclipse.e4.ui.model.application.MPSCElement;
import org.eclipse.e4.ui.model.application.MPart;
import org.eclipse.e4.ui.model.application.MPartStack;
import org.eclipse.e4.ui.model.application.MUIElement;
import org.eclipse.e4.ui.model.application.MWindow;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.workbench.ui.IPresentationEngine;
import org.eclipse.e4.workbench.ui.UIEvents;
import org.eclipse.e4.workbench.ui.internal.Workbench;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Utilities for headless testing.
 * @author Fabian Steeg (fsteeg)
 */
final class HeadlessSetup {
  private HeadlessSetup() {}

  static final IRunAndTrack RUNNABLE = new IRunAndTrack() {
    public boolean notify(final ContextChangeEvent event) {
      IEclipseContext eventsContext = event.getContext();
      Object o = eventsContext.get(IServiceConstants.ACTIVE_PART);
      if (o instanceof MPart) {
        eventsContext.set(IServiceConstants.ACTIVE_PART_ID, ((MPart) o).getId());
      }
      return true;
    }

    @Override public String toString() {
      return "HeadlessStartupTest$RunAndTrack[" + IServiceConstants.ACTIVE_PART_ID + ']';
    }
  };

  static MApplicationElement createApplicationElement(final IEclipseContext appContext,
      final String appUri) throws Exception {
    return new HeadlessSetup().createApplication(appContext, appUri);
  }

  static MPart findElement(final String id, final MElementContainer<?> application) {
    return (MPart) findElement(application, id);
  }

  static MApplicationElement findElement(final MElementContainer<?> container, final String id) {
    if (id.equals(container.getId())) {
      return container;
    }
    for (Object child : container.getChildren()) {
      MApplicationElement element = (MApplicationElement) child;
      if (element instanceof MElementContainer<?>) {
        MApplicationElement found = findElement((MElementContainer<?>) element, id);
        if (found != null) {
          return found;
        }
      } else if (id.equals(element.getId())) {
        return element;
      }
    }
    return null;
  }

  private MApplication createApplication(final IEclipseContext appContext, final String appURI)
      throws Exception {
    URI initialWorkbenchDefinitionInstance = URI.createPlatformPluginURI(appURI, true);
    ResourceSet set = new ResourceSetImpl();
    set.getPackageRegistry().put("http://MApplicationPackage/", MApplicationPackage.eINSTANCE);
    Resource resource = set.getResource(initialWorkbenchDefinitionInstance, true);
    MApplication application = (MApplication) resource.getContents().get(0);
    application.setContext(appContext);
    appContext.set(MApplication.class.getName(), application);
    defineCommands(appContext, application);
    initializeContexts(appContext, application);
    Workbench.processHierarchy(application);
    processPartContributions(application.getContext(), resource);
    return application;
  }

  private void initializeContexts(final IEclipseContext appContext, final MApplication application) {
    for (MWindow window : application.getChildren()) {
      Workbench.initializeContext(appContext, window);
    }
  }

  private void defineCommands(final IEclipseContext appContext, final MApplication application) {
    ECommandService cs = (ECommandService) appContext.get(ECommandService.class.getName());
    Category cat = cs.defineCategory(MApplication.class.getName(), "Application Category", null); //$NON-NLS-1$
    for (MCommand cmd : application.getCommands()) {
      String id = cmd.getId();
      String name = cmd.getCommandName();
      cs.defineCommand(id, name, null, cat, null);
    }
  }

  private void processPartContributions(final IEclipseContext context, final Resource resource) {
    IExtensionRegistry registry = (IExtensionRegistry) context.get(IExtensionRegistry.class
        .getName());
    String extId = "org.eclipse.e4.workbench.parts"; //$NON-NLS-1$
    IConfigurationElement[] parts = registry.getConfigurationElementsFor(extId);

    for (int i = 0; i < parts.length; i++) {
      MPart part = MApplicationFactory.eINSTANCE.createPart();
      part.setLabel(parts[i].getAttribute("label")); //$NON-NLS-1$
      part.setIconURI("platform:/plugin/" //$NON-NLS-1$
          + parts[i].getContributor().getName() + "/" //$NON-NLS-1$
          + parts[i].getAttribute("icon")); //$NON-NLS-1$
      part.setURI("platform:/plugin/" //$NON-NLS-1$
          + parts[i].getContributor().getName() + "/" //$NON-NLS-1$
          + parts[i].getAttribute("class")); //$NON-NLS-1$
      String parentId = parts[i].getAttribute("parentId"); //$NON-NLS-1$

      Object parent = findObject(resource.getAllContents(), parentId);
      if (parent instanceof MElementContainer<?>) {
        ((MElementContainer<MPSCElement>) parent).getChildren().add(part);
      }
    }
  }

  private EObject findObject(final TreeIterator<EObject> it, final String id) {
    while (it.hasNext()) {
      EObject el = it.next();
      if (el instanceof MApplicationElement) {
        if (el.eResource().getURIFragment(el).equals(id)) {
          return el;
        }
      }
    }
    return null;
  }

  static final String ENGINE_URI = HeadlessEngine.ENGINE_URI;

  public static class HeadlessEngine implements IPresentationEngine {

    private static final String NAME = HeadlessEngine.class.getPackage().getName();
    static final String ENGINE_URI = "platform:/plugin/" + NAME + "/"
        + HeadlessEngine.class.getName();
    @Inject private org.eclipse.e4.ui.services.events.IEventBroker eventBroker;
    @Inject private IContributionFactory contributionFactory;
    private EventHandler childHandler;
    private EventHandler activeChildHandler;

    @Override public Object createGui(final MUIElement element, final Object parent) {
      if (element instanceof MContext) {
        MContext mcontext = (MContext) element;
        if (mcontext.getContext() != null) {
          return null;
        }
        setupContext(element, mcontext);
      }
      if (element instanceof MPartStack) {
        createGuiFromPartStack(element);
      } else if (element instanceof MElementContainer<?>) {
        createGuiFromChildren(element);
      }
      return null;
    }

    @Override public Object createGui(final MUIElement element) {
      return createGui(element, null);
    }

    @Override public void removeGui(final MUIElement element) {
      if (element instanceof MElementContainer<?>) {
        for (Object child : ((MElementContainer<?>) element).getChildren()) {
          if (child instanceof MUIElement) {
            removeGui((MUIElement) child);
          }
        }
      }
      if (element instanceof MContext) {
        MContext mcontext = (MContext) element;
        IEclipseContext context = mcontext.getContext();
        mcontext.setContext(null);
        if (context instanceof IDisposable) {
          ((IDisposable) context).dispose();
        }
      }
    }

    @Override public Object run(final MApplicationElement uiRoot, final IEclipseContext appContext) {
      return null;
    }

    @Override public void stop() {}

    @PostConstruct void postConstruct() {
      childHandler = new EventHandler() {
        public void handleEvent(final Event event) {
          if (UIEvents.EventTypes.ADD.equals(event.getProperty(UIEvents.EventTags.TYPE))) {
            Object element = event.getProperty(UIEvents.EventTags.NEW_VALUE);
            if (element instanceof MUIElement) {
              Object parent = event.getProperty(UIEvents.EventTags.ELEMENT);
              createGui((MUIElement) element, parent);
              if (parent instanceof MPartStack) {
                MPartStack stack = (MPartStack) parent;
                List<MPart> children = stack.getChildren();
                if (children.size() == 1) {
                  stack.setSelectedElement((MPart) element);
                }
              }
            }
          }
        }
      };

      eventBroker.subscribe(UIEvents.buildTopic(UIEvents.ElementContainer.TOPIC,
          UIEvents.ElementContainer.CHILDREN), childHandler);

      activeChildHandler = new EventHandler() {
        public void handleEvent(final Event event) {
          Object element = event.getProperty(UIEvents.EventTags.NEW_VALUE);
          if (element instanceof MUIElement) {
            Object parent = event.getProperty(UIEvents.EventTags.ELEMENT);
            createGui((MUIElement) element, parent);
          }
        }
      };

      eventBroker.subscribe(UIEvents.buildTopic(UIEvents.ElementContainer.TOPIC,
          UIEvents.ElementContainer.SELECTEDELEMENT), activeChildHandler);
    }

    private void createGuiFromChildren(final MUIElement element) {
      for (Object child : ((MElementContainer<?>) element).getChildren()) {
        if (child instanceof MUIElement) {
          createGui((MUIElement) child, element);
          if (child instanceof MContext) {
            IEclipseContext childContext = ((MContext) child).getContext();
            IEclipseContext parentContext = getParentContext((MUIElement) child);
            if (parentContext.getLocal(IContextConstants.ACTIVE_CHILD) == null) {
              parentContext.set(IContextConstants.ACTIVE_CHILD, childContext);
            }
          }
        }
      }
    }

    private void createGuiFromPartStack(final MUIElement element) {
      MPartStack container = (MPartStack) element;
      MPart active = container.getSelectedElement();
      if (active != null) {
        createGui(active, container);
        IEclipseContext childContext = ((MContext) active).getContext();
        IEclipseContext parentContext = getParentContext(active);
        parentContext.set(IContextConstants.ACTIVE_CHILD, childContext);
      } else {
        List<MPart> children = container.getChildren();
        if (!children.isEmpty()) {
          container.setSelectedElement(children.get(0));
        }
      }
    }

    private void setupContext(final MUIElement element, final MContext mcontext) {
      final IEclipseContext parentContext = getParentContext(element);
      final IEclipseContext createdContext = EclipseContextFactory.create(parentContext, null);
      createdContext.set(IContextConstants.DEBUG_STRING, element.getClass().getInterfaces()[0]
          .getName()
          + " eclipse context"); //$NON-NLS-1$
      populateModelInterfaces(mcontext, createdContext, element.getClass().getInterfaces());
      for (String variable : mcontext.getVariables()) {
        createdContext.declareModifiable(variable);
      }
      mcontext.setContext(createdContext);
      if (element instanceof MContribution) {
        MContribution contribution = (MContribution) element;
        String uri = contribution.getURI();
        if (uri != null) {
          Object clientObject = contributionFactory.create(uri, createdContext);
          contribution.setObject(clientObject);
        }
      }
      if (parentContext.getLocal(IContextConstants.ACTIVE_CHILD) == null) {
        parentContext.set(IContextConstants.ACTIVE_CHILD, createdContext);
      }
    }

    private static IEclipseContext getParentContext(final MUIElement element) {
      MElementContainer<MUIElement> parent = element.getParent();
      IEclipseContext context = null;
      while (parent != null) {
        if (parent instanceof MContext) {
          return ((MContext) parent).getContext();
        }
        parent = parent.getParent();
      }
      return context;
    }

    private static void populateModelInterfaces(final MContext contextModel,
        final IEclipseContext context, final Class<?>[] interfaces) {
      for (Class<?> intf : interfaces) {
        context.set(intf.getName(), contextModel);
        populateModelInterfaces(contextModel, context, intf.getInterfaces());
      }
    }
  }
}
