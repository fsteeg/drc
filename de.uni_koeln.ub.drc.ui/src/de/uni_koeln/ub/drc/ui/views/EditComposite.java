/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.views;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

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

import scala.collection.JavaConversions;
import de.uni_koeln.ub.drc.data.Modification;
import de.uni_koeln.ub.drc.data.Page;
import de.uni_koeln.ub.drc.data.Word;
import de.uni_koeln.ub.drc.ui.DrcUiActivator;

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

  @Inject
  public EditComposite(final MDirtyable dirtyable, final Composite parent, final int style) {
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
      @Override
      public void controlResized(final ControlEvent e) {
        for (Composite line : lines) {
          if (!line.isDisposed()) {
            setLineLayout(line);
          }
        }
        layout();
      }

      @Override
      public void controlMoved(final ControlEvent e) {}
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
      if (word.original().equals(Page.ParagraphMarker())) {
        lineComposite = new Composite(c, SWT.NONE);
        setLineLayout(lineComposite);
        lines.add(lineComposite);
      } else {
        Text text = new Text(lineComposite, SWT.NONE);
        text.setText(word.history().top().form());
        if (word.isPossibleError()) {
          text.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
        }
        text.setData(word);
        addListeners(text);
        text.setEditable(!word.isLocked());
        list.add(text);
      }
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

  private Text prev;
  private int active = SWT.COLOR_DARK_GREEN;
  private int dubious = SWT.COLOR_RED;
  
  private void addModifyListener(final Text text) {
    text.addModifyListener(new ModifyListener() {
      public void modifyText(final ModifyEvent e) {
        /* Reset any warning color during editing (we check when focus is lost, see below): */
        text.setForeground(text.getDisplay().getSystemColor(active));
        if (commitChanges) {
          dirtyable.setDirty(true);
        }
      }
    });
  }

  private void addFocusListener(final Text text) {
    text.addFocusListener(new FocusListener() {
      public void focusLost(final FocusEvent e) {
        prev = text; // remember so we can clear only when new focus gained, not when lost
        context.modify(IServiceConstants.ACTIVE_SELECTION, null);
        checkWordValidity(text);
      }

      private void checkWordValidity(final Text text) {
        String current = text.getText();
        Word word = (Word) text.getData();
        String reference = word.history().top().form();
        if (current.length() != reference.length()
            || (current.contains(" ") && !reference.contains(" "))) {
          if(!MessageDialog.openQuestion(text.getShell(), "Questionable Edit Operation",
              "Your recent edit operation changed a word in a dubious way (e.g. by adding a blank into "
                  + "what should be a single word or by changing the length of a word) - are you sure?")){
            text.setForeground(text.getDisplay().getSystemColor(dubious));
          } else {
            word.history().push(new Modification(current, DrcUiActivator.instance().currentUser().id()));
          }
        }
      }

      public void focusGained(final FocusEvent e) {
        text.clearSelection(); // use only our persistent marking below
        context.modify(IServiceConstants.ACTIVE_SELECTION, text);
        text.setToolTipText(((Word) text.getData()).formattedHistory());
        if (prev != null && !prev.isDisposed()
            && !prev.getForeground().equals(text.getDisplay().getSystemColor(dubious))) {
          prev.setForeground(text.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        }
        text.setForeground(text.getDisplay().getSystemColor(active));
      }
    });
  }
}
