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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;

import scala.collection.JavaConversions;
import de.uni_koeln.ub.drc.data.Page;
import de.uni_koeln.ub.drc.data.Word;

/**
 * Composite holding the edit area. Used by the {@link EditView}.
 * @author Fabian Steeg (fsteeg)
 */
public final class EditComposite extends Composite {

  private MDirtyable dirtyable;
  private Composite parent;
  private Page page;
  private boolean commitChanges = false;
  private List<Text> words;
  private List<Composite> lines = new ArrayList<Composite>();
  IEclipseContext context;

  @Inject public EditComposite(final MDirtyable dirtyable, final Composite parent, final int style) {
    super(parent, style);
    this.parent = parent;
    this.dirtyable = dirtyable;
    parent.getShell().setBackgroundMode(SWT.INHERIT_DEFAULT);
    GridLayout layout = new GridLayout(1, false);
    this.setLayout(layout);
    commitChanges = true;
    addWrapOnResizeListener(parent);
  }

  /**
   * @return The text widgets representing the words in the current page
   */
  public List<Text> getWords() {
    return words;
  }

  /**
   * @param page Update the content of this composite with the given page
   */
  public void update(final Page page) {
    this.page = page;
    updatePage(parent);
  }

  Page getPage() {
    return page;
  }

  private void addWrapOnResizeListener(final Composite parent) {
    parent.addControlListener(new ControlListener() {
      @Override public void controlResized(final ControlEvent e) {
        for (Composite line : lines) {
          if (!line.isDisposed()) {
            setLineLayout(line);
          }
        }
        layout();
      }

      @Override public void controlMoved(final ControlEvent e) {}
    });
  }

  private void updatePage(final Composite parent) {
    words = addTextFrom(page, this);
  }

  private List<Text> addTextFrom(final Page page, final Composite c) {
    if (lines != null) {
      for (Composite line : lines) {
        line.dispose();
      }
    }
    List<Text> list = new ArrayList<Text>();
    this.page = page;
    Composite lineComposite = new Composite(c, SWT.NONE);
    setLineLayout(lineComposite);
    lines.add(lineComposite);
    for (Word word : JavaConversions.asIterable(page.words())) {
      Text text = new Text(lineComposite, SWT.NONE);
      if (word.original().equals("@")) {
        lineComposite = new Composite(c, SWT.NONE);
        setLineLayout(lineComposite);
        lines.add(lineComposite);
      } else {
        text.setText(word.history().top().form());
        if (word.isPossibleError()) {
          text.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        }
      }
      text.setData(word);
      addListeners(text);
      list.add(text);
    }
    this.layout();
    return list;
  }

  private void setLineLayout(final Composite lineComposite) {
    RowLayout layout = new RowLayout();
    GridData data = new GridData();
    data.widthHint = lineComposite.computeSize(parent.getSize().x, parent.getSize().y).x - 20;
    lineComposite.setLayoutData(data);
    lineComposite.setLayout(layout);
  }

  private void addListeners(final Text text) {
    addFocusListener(text);
    addModifyListener(text);
  }

  private void addModifyListener(final Text text) {
    text.addModifyListener(new ModifyListener() {
      public void modifyText(final ModifyEvent e) {
        /* Reset any warning color during editing (we check when focus is lost, see below): */
        text.setForeground(text.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        if (commitChanges) {
          dirtyable.setDirty(true);
        }
      }
    });
  }

  private void addFocusListener(final Text text) {
    text.addFocusListener(new FocusListener() {
      public void focusLost(final FocusEvent e) {
        context.modify(IServiceConstants.SELECTION, null);
        checkWordValidity(text);
      }

      private void checkWordValidity(final Text text) {
        String current = text.getText();
        Word word = (Word) text.getData();
        if (current.length() != word.original().length() || current.contains(" ")) {
          text.setForeground(text.getDisplay().getSystemColor(SWT.COLOR_RED));
          MessageDialog.openWarning(text.getShell(), "Questionable Edit Operation",
              "Your recent edit operation changed a word in a dubious way (e.g. by adding a blank into "
                  + "what should be a single word or by changing the length of a word) - it has "
                  + "been marked red");
        }
      }

      public void focusGained(final FocusEvent e) {
        context.modify(IServiceConstants.SELECTION, text);
        text.setToolTipText(((Word) text.getData()).formattedHistory());
      }
    });
  }

  static File fileFromBundle(final String location) { // TODO move to a utils class
    Bundle bundle = Platform.getBundle("de.uni_koeln.ub.drc.ui"); //$NON-NLS-1$
    try {
      URL resource = bundle.getResource(location);
      if (resource == null) {
        System.err.println("Could not resolve: " + location);
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
}
