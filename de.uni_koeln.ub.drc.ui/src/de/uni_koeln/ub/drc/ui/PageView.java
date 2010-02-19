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
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.services.annotations.Optional;
import org.eclipse.e4.ui.model.application.MDirtyable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import scala.collection.JavaConversions;
import scala.collection.mutable.Stack;
import de.uni_koeln.ub.drc.data.Modification;
import de.uni_koeln.ub.drc.data.Page;
import de.uni_koeln.ub.drc.data.Word;

/**
 * An experimental view that displays the original, scanned image file and a space to edit the
 * corresponding OCR'ed text, marking the section in the image file that corresponds to the word in
 * focus. Currently mock dummy test data.
 * @author Fabian Steeg (fsteeg)
 */
public final class PageView {

  private final MDirtyable dirtyable;

  private final PageComposite pageComposite;

  @Inject
  public PageView(final Composite parent, final MDirtyable dirtyable) {
    pageComposite = new PageComposite(dirtyable, parent, SWT.NONE);
    this.dirtyable = dirtyable;
    parent.getShell().setLayout(new FillLayout());
    // GridLayoutFactory.fillDefaults().generateLayout(parent);
  }
  
  public boolean isSaveOnCloseNeeded() {
    return true;
  }

  public void doSave(@Optional final IProgressMonitor m) throws IOException, InterruptedException {
    final IProgressMonitor monitor = m == null ? new NullProgressMonitor() : m;
    final Page page = pageComposite.getPage();
    monitor.beginTask("Saving page...", page.words().size());
    final Iterator<Word> modified = JavaConversions.asIterable(page.words()).iterator();
    final List<Text> words = pageComposite.getWords();
    
    pageComposite.getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        for (int i = 0; i < words.size(); i++) {
          String newText = words.get(i).getText();
          Stack<Modification> history = modified.next().history();
          String oldText = history.top().form();
          if (!newText.equals(oldText)) {
            history.push(new Modification(newText, System.getProperty("user.name")));
          }
          monitor.worked(1);
        }
        saveToXml(page);
      }
    });
    
    dirtyable.setDirty(false);
  }

  private void saveToXml(final Page page) {
    File file = pageComposite.getFile();
    System.out.println("Saving to: " + file);
    page.save(file);
  }

}
