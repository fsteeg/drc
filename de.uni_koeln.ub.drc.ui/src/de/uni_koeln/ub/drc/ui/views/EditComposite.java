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
import org.eclipse.e4.ui.css.swt.CSSSWTConstants;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.services.IServiceConstants;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import scala.collection.JavaConversions;
import de.uni_koeln.ub.drc.data.Page;
import de.uni_koeln.ub.drc.data.User;
import de.uni_koeln.ub.drc.data.Word;
import de.uni_koeln.ub.drc.ui.DrcUiActivator;
import de.uni_koeln.ub.drc.ui.Messages;

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
  private Label status;

  @Inject
  public EditComposite(final EditView editView, final int style) {
    super(editView.sc, style);
    this.parent = editView.sc;
    this.dirtyable = editView.dirtyable;
    this.status = editView.label;
    parent.getShell().setBackgroundMode(SWT.INHERIT_DEFAULT);
    GridLayout layout = new GridLayout(1, false);
    this.setLayout(layout);
    commitChanges = true;
    addWrapOnResizeListener(parent);
    setCssName(this);
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
        setCssName(text);
        text.setText(word.history().top().form());
        text.setForeground(parent.getDisplay().getSystemColor(
            word.isPossibleError() ? UNCHECKED : DEFAULT));
        text.setData(Word.class.toString(), word);
        text.setData(Page.class.toString(), page);
        addListeners(text);
        text.setEditable(!word.isLocked());
        list.add(text);
      }
    }
    this.layout();
    return list;
  }

  private void setCssName(Control control) {
    control.setData(CSSSWTConstants.CSS_CLASS_NAME_KEY, "editComposite"); //$NON-NLS-1$
  }

  private void setLineLayout(final Composite lineComposite) {
    setCssName(lineComposite);
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
  final static int ACTIVE = SWT.COLOR_DARK_GREEN;
  final static int DUBIOUS = SWT.COLOR_RED;
  final static int DEFAULT = SWT.COLOR_BLACK;
  final static int UNCHECKED = SWT.COLOR_BLACK; // no coloring for now

  private void addModifyListener(final Text text) {
    text.addModifyListener(new ModifyListener() {
      public void modifyText(final ModifyEvent e) {
        User user = DrcUiActivator.instance().currentUser();
        user.latestPage_$eq(page.id());
        user.latestWord_$eq(words.indexOf(text));
        /* Reset any warning color during editing (we check when focus is lost, see below): */
        text.setForeground(text.getDisplay().getSystemColor(ACTIVE));
        if (commitChanges) {
          dirtyable.setDirty(true);
        }
        text.pack(true);
        text.getParent().layout();
        /*
         * Workaround: on Windows, when adding text at the end of the widget, text at the beginning
         * is pushed out of the widget and not visible - so we jump to beginning and then back:
         */
        int pos = text.getCaretPosition();
        text.setSelection(0);
        text.setSelection(pos);
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
        Word word = (Word) text.getData(Word.class.toString());
        String reference = word.history().top().form();
        if (current.length() != reference.length()
            || (current.contains(" ") && !reference.contains(" "))) { //$NON-NLS-1$ //$NON-NLS-2$
          text.setForeground(text.getDisplay().getSystemColor(DUBIOUS));
          setMessage(Messages.YourRecentEdit);
        } else {
          status.setText(""); //$NON-NLS-1$
        }
      }

      public void focusGained(final FocusEvent e) {
        text.clearSelection(); // use only our persistent marking below
        Word word = (Word) text.getData(Word.class.toString());
        if (word.isLocked()) {
          setMessage(String.format(Messages.Entry + " '%s' " + Messages.IsLocked, text.getText())); //$NON-NLS-2$
        }
        text.setEditable(!word.isLocked());
        context.modify(IServiceConstants.ACTIVE_SELECTION, text);
        text.setToolTipText(word.formattedHistory());
        if (prev != null && !prev.isDisposed()
            && !prev.getForeground().equals(text.getDisplay().getSystemColor(DUBIOUS))) {
          prev.setForeground(text.getDisplay().getSystemColor(DEFAULT));
        }
        text.setForeground(text.getDisplay().getSystemColor(ACTIVE));
      }

      private void setMessage(String t) {
        status.setText(t);
        status.setForeground(status.getDisplay().getSystemColor(DUBIOUS));
      }
    });
  }
}
