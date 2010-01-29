/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.services.Logger;
import org.eclipse.e4.core.services.annotations.PostConstruct;
import org.eclipse.e4.core.services.context.IEclipseContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.uni_koeln.ub.drc.ui.Word.Modification;

/**
 * An experimental view that displays the original, scanned image file and a space to edit the
 * corresponding OCR'ed text, marking the section in the image file that corresponds to the word in
 * focus. Currently mock dummy test data.
 * @author Fabian Steeg (fsteeg)
 */
public class ImageView {

  private static final String IMAGE = "00000007.jpg";

  @Inject
  private Logger logger;
  @Inject
  private IEclipseContext context;
  @Inject
  private Composite parent;

  @PostConstruct
  // = after everything is injected
  public void init() {
    logger.info("Initializing DRC e4 UI");
    logger.info("Context: " + context);
    parent.getShell().setLayout(new FillLayout());
    final Image image = loadImage();
    final Label label = new Label(parent, SWT.BORDER);
    label.setImage(image);
    Composite c = new Composite(parent, SWT.NONE);
    c.setSize(600, 960);
    c.setLayout(new GridLayout(10, false));
    addTextFrom(Word.testData(), c, label);
    parent.getShell().setSize(1024, 960);
  }

  private void addTextFrom(List<Word> words, Composite c, Label label) {
    for (Word word : words) {
      Text text = new Text(c, SWT.BORDER);
      text.setText(word.history.peek().form);
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
      public void modifyText(ModifyEvent e) {
        /* Update the current form of the word associated with the text widget: */
        Word word = (Word) text.getData();
        String textContent = text.getText();
        if (textContent.length() != word.original.length()) {
          text.setForeground(text.getDisplay().getSystemColor(SWT.COLOR_RED));
        } else text.setForeground(text.getDisplay().getSystemColor(SWT.COLOR_BLACK));
        /* This is for testing; later this would only be done upon saving. */
        word.history.push(new Modification(textContent, System.getProperty("user.name")));
      }
    });
  }

  private void addFocusListener(final Label label, final Text text) {
    text.addFocusListener(new FocusListener() {
      public void focusLost(FocusEvent e) {
        clearMarker(label);
      }
      public void focusGained(FocusEvent e) {
        markPosition(label, text);
        text.setToolTipText(((Word)text.getData()).formattedHistory());
      }
    });
  }

  private void markPosition(final Label label, final Text text) {
    Word word = (Word) text.getData();
    logger.info("Current word: " + word);
    Image image = loadImage();
    GC gc = new GC(image);
    gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
    Rectangle rect = word.position;
    if (rect != null) {
      gc.drawRectangle(rect);
    }
    label.setImage(image);
  }

  protected void clearMarker(Label label) {
    Image image = loadImage();
    label.setImage(image);
  }

  private Image loadImage() {
    Display display = parent.getDisplay();
    Image newImage = new Image(display, ImageView.class.getResourceAsStream(IMAGE));
    return newImage;
  }

}
