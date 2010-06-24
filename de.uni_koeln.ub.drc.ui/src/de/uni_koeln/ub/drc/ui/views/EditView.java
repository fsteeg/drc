/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.views;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.auth.login.LoginException;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import scala.collection.JavaConversions;
import scala.collection.mutable.Stack;
import de.uni_koeln.ub.drc.data.Modification;
import de.uni_koeln.ub.drc.data.Page;
import de.uni_koeln.ub.drc.data.Word;
import de.uni_koeln.ub.drc.ui.DrcUiActivator;

/**
 * A view that the area to edit the text. Marks the section in the image file that corresponds to
 * the word in focus (in {@link CheckView}).
 * @author Fabian Steeg (fsteeg)
 */
public final class EditView {

  @Inject private EHandlerService handlerService;
  @Inject private ECommandService commandService;
  @Inject private IEclipseContext context;

  private final MDirtyable dirtyable;
  private final EditComposite editComposite;

  @PostConstruct public void setContext() {
    editComposite.context = context; // FIXME this can't be right
  }

  @Inject public EditView(final Composite parent, final MDirtyable dirtyable) {
    ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.BORDER);
    editComposite = new EditComposite(dirtyable, sc, SWT.NONE);
    sc.setContent(editComposite);
    sc.setExpandVertical(true);
    sc.setExpandHorizontal(true);
    sc.setMinSize(editComposite.computeSize(SWT.MAX, SWT.MAX));
    this.dirtyable = dirtyable;
  }

  @Inject public void setSelection(
      @Optional @Named( IServiceConstants.ACTIVE_SELECTION ) final List<Page> pages) {
    if (pages != null && pages.size() > 0) {
      Page page = pages.get(0);
      if (dirtyable.isDirty()) {
        MessageDialog dialog = new MessageDialog(editComposite.getShell(), "Save page", null,
            "The current page has been modified. Save changes?", MessageDialog.CONFIRM,
            new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL }, 0);
        dialog.create();
        if (dialog.open() == Window.OK) {
          ParameterizedCommand saveCommand = commandService.createCommand("page.save",
              Collections.EMPTY_MAP);
          handlerService.executeHandler(saveCommand);
        }
      }
      editComposite.update(page);
    } else {
      return;
    }
    dirtyable.setDirty(false);
  }

  @Persist public void doSave(@Optional final IProgressMonitor m) throws IOException,
      InterruptedException {
    final IProgressMonitor monitor = m == null ? new NullProgressMonitor() : m;
    final Page page = editComposite.getPage();
    monitor.beginTask("Saving page...", page.words().size());
    final Iterator<Word> modified = JavaConversions.asIterable(page.words()).iterator();
    final List<Text> words = editComposite.getWords();

    editComposite.getDisplay().asyncExec(new Runnable() {
      @Override public void run() {
        for (int i = 0; i < words.size(); i++) {
          String newText = words.get(i).getText();
          Word word = modified.next();
          Stack<Modification> history = word.history();
          String oldText = history.top().form();
          if (!newText.equals(oldText) && !word.original().trim().equals(Page.ParagraphMarker())) {
            try {
              history.push(new Modification(newText, DrcUiActivator.instance().getLoginContext()
                  .getSubject().getPublicCredentials().iterator().next().toString()));
            } catch (LoginException e) {
              e.printStackTrace();
            } /* System.getProperty("user.name") */
          }
          monitor.worked(1);
        }
        saveToXml(page);
      }
    });
    dirtyable.setDirty(false);
  }

  public boolean isSaveOnCloseNeeded() {
    return true;
  }

  private void saveToXml(final Page page) {
    System.out.println("Saving page: " + page);
    page.save();
  }
}
