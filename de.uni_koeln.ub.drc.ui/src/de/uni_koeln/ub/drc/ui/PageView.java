/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui;

import java.io.IOException;

import javax.inject.Inject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.core.services.annotations.Optional;
import org.eclipse.e4.ui.model.application.MDirtyable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import scala.collection.JavaConversions;
import de.uni_koeln.ub.drc.data.Page;
import de.uni_koeln.ub.drc.data.Word;

/**
 * An experimental view that displays the original, scanned image file and a space to edit the
 * corresponding OCR'ed text, marking the section in the image file that corresponds to the word in
 * focus. Currently mock dummy test data.
 * @author Fabian Steeg (fsteeg)
 */
public class PageView {

  private final MDirtyable dirtyable;

  private final PageComposite pageComposite;

  @Inject
  public PageView(final Composite parent, final MDirtyable dirtyable) {
    pageComposite = new PageComposite(dirtyable, parent, SWT.NONE);
    this.dirtyable = dirtyable;
    parent.getShell().setLayout(new FillLayout());
    // GridLayoutFactory.fillDefaults().generateLayout(parent);
  }

  public void doSave(@Optional IProgressMonitor monitor) throws IOException, InterruptedException {
    if (monitor == null) {
      monitor = new NullProgressMonitor();
    }
    Page originalPage = pageComposite.getOriginalPage();
    Page modifiedPage = pageComposite.getModifiedPage();
    monitor.beginTask("Saving page...", modifiedPage.words().size());
    saveToXml(modifiedPage);
    Iterable<Word> modified = JavaConversions.asIterable(modifiedPage.words());
    for (Word word : modified) {
      System.out.println("Would need to update word: " + word + " on original page: "
          + originalPage);
      // TODO add recent modification to the word
      Thread.sleep(50);
      monitor.worked(1);
    }
    dirtyable.setDirty(false);
  }

  private void saveToXml(final Page modifiedPage) {
    System.out.println("Would save: \n" + modifiedPage.toXml());
  }

  public boolean isSaveOnCloseNeeded() {
    return true;
  }

}
