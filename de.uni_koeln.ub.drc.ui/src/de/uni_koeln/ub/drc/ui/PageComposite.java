/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui;

import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.MDirtyable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import scala.collection.JavaConversions;
import de.uni_koeln.ub.drc.data.Box;
import de.uni_koeln.ub.drc.data.Modification;
import de.uni_koeln.ub.drc.data.Page;
import de.uni_koeln.ub.drc.data.Word;

/**
 * Composite holding the scanned image and the edit area. Used by the {@link PageView}.
 * @author Fabian Steeg (fsteeg)
 */
public final class PageComposite extends Composite {

  private static final String IMAGE = "00000007.jpg";
  private MDirtyable dirtyable;
  private Composite parent;
  private Page modifiedPage;
  private Page originalPage;
  private boolean commitChanges = false;

  @Inject
  public PageComposite(final MDirtyable dirtyable, final Composite parent, final int style) {
    super(parent, style);
    this.parent = parent;
    this.dirtyable = dirtyable;
    parent.getShell().setBackgroundMode(SWT.INHERIT_DEFAULT);
    final Image image = loadImage();
    final Label label = new Label(parent, SWT.BORDER);
    label.setImage(image);
    this.setSize(600, 960);
    this.setLayout(new GridLayout(10, false));
    addTextFrom(Page.mock(), this, label);
    parent.getShell().setSize(1024, 960);
    commitChanges = true;
  }

  private void addTextFrom(final Page page, final Composite c, final Label label) {
    originalPage = page;
    modifiedPage = page;
    for (Word word : JavaConversions.asIterable(page.words())) {
      Text text = new Text(c, SWT.BORDER);
      text.setText(word.history().top().form());
      text.setData(word);
      addListeners(label, text);
    }
  }

  private void addListeners(final Label label, final Text text) {
    addFocusListener(label, text);
    addModifyListener(text);
  }

  private void addModifyListener(final Text text) {
    text.addModifyListener(new ModifyListener() {
      public void modifyText(final ModifyEvent e) {
        /* Update the current form of the word associated with the text widget: */
        Word word = (Word) text.getData();
        String textContent = text.getText();
        if (textContent.length() != word.original().length()) {
          text.setForeground(text.getDisplay().getSystemColor(SWT.COLOR_RED));
        } else {
          text.setForeground(text.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        }
        /* This is for testing; later this would only be done upon saving. */
        word.history().push(new Modification(textContent, System.getProperty("user.name")));
        if (commitChanges) {
          dirtyable.setDirty(true);
        }
      }
    });
  }

  private void addFocusListener(final Label label, final Text text) {
    text.addFocusListener(new FocusListener() {
      public void focusLost(final FocusEvent e) {
        clearMarker(label);
      }

      public void focusGained(final FocusEvent e) {
        markPosition(label, text);
        text.setToolTipText(((Word) text.getData()).formattedHistory());
      }
    });
  }

  private void markPosition(final Label label, final Text text) {
    Word word = (Word) text.getData();
    System.out.println("Current word: " + word);
    Image image = loadImage();
    GC gc = new GC(image);
    gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
    Box box = word.position();
    Rectangle rect = new Rectangle(box.x(), box.y(), box.width(), box.height());
    if (rect != null) {
      gc.drawRectangle(rect);
    }
    label.setImage(image);
  }

  private void clearMarker(final Label label) {
    Image image = loadImage();
    label.setImage(image);
  }

  private Image loadImage() {
    Display display = parent.getDisplay();
    Image newImage = new Image(display, PageView.class.getResourceAsStream(IMAGE));
    return newImage;
  }

  Page getOriginalPage() {
    return originalPage;
  }

  Page getModifiedPage() {
    return modifiedPage;
  }

}
