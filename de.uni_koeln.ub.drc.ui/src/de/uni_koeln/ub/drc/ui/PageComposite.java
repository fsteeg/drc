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
import org.osgi.framework.Bundle;

import scala.collection.JavaConversions;
import de.uni_koeln.ub.drc.data.Box;
import de.uni_koeln.ub.drc.data.Page;
import de.uni_koeln.ub.drc.data.Word;

/**
 * Composite holding the scanned image and the edit area. Used by the {@link PageView}.
 * @author Fabian Steeg (fsteeg)
 */
public final class PageComposite extends Composite {

  private URL xmlFile;
  private URL jpgFile;
  private MDirtyable dirtyable;
  private Composite parent;
  private Page page;
  private boolean commitChanges = false;
  private List<Text> words;

  @Inject
  public PageComposite(final MDirtyable dirtyable, final Composite parent, final int style) {
    super(parent, style);
    loadStoredPage();
    this.parent = parent;
    this.dirtyable = dirtyable;
    parent.getShell().setBackgroundMode(SWT.INHERIT_DEFAULT);
    final Image image = loadImage();
    final Label label = new Label(parent, SWT.BORDER);
    label.setImage(image);
    this.setSize(600, 960);
    this.setLayout(new GridLayout(10, false));
    try {
      Page page = Page.load(xmlFile.openStream());
      words = addTextFrom(page, this, label);
    } catch (IOException e) {
      e.printStackTrace();
    }
    parent.getShell().setSize(1024, 960);
    commitChanges = true;
  }
  
  /**
   * @return The XML file holding the content of the page being edited
   */
  public File getFile() {
    try {
      return new File(xmlFile.toURI());
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * @return The text widgets representing the words in the current page
   */
  public List<Text> getWords() {
    return words;
  }
  
  Page getPage() {
    return page;
  }

  private void loadStoredPage() {
    Bundle bundle = Platform.getBundle("de.uni-koeln.ub.drc.ui"); //$NON-NLS-1$
    try {
      xmlFile = FileLocator.resolve((URL) bundle.getResources("pages/drc-page.xml").nextElement());
      jpgFile = FileLocator.resolve((URL) bundle.getResources("pages/drc-page.jpg").nextElement());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private List<Text> addTextFrom(final Page page, final Composite c, final Label label) {
    List<Text> list = new ArrayList<Text>();
    this.page = page;
    for (Word word : JavaConversions.asIterable(page.words())) {
      Text text = new Text(c, SWT.BORDER);
      text.setText(word.history().top().form());
      text.setData(word);
      addListeners(label, text);
      list.add(text);
    }
    return list;
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
    Image newImage = null;
    try {
      newImage = new Image(display, jpgFile.openStream());
    } catch (IOException e) {
      e.printStackTrace();
    }
    return newImage;
  }

}
