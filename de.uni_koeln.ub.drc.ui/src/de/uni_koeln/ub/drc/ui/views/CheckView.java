/**************************************************************************************************
 * Copyright (c) 2010 Fabian Steeg. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors: Fabian Steeg - initial API and implementation
 *************************************************************************************************/
package de.uni_koeln.ub.drc.ui.views;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.uni_koeln.ub.drc.data.Box;
import de.uni_koeln.ub.drc.data.Index;
import de.uni_koeln.ub.drc.data.Page;
import de.uni_koeln.ub.drc.data.Word;

/**
 * View containing the scanned page used to check the original word while editing.
 * @author Fabian Steeg (fsteeg)
 */
public final class CheckView {
  private Composite parent;
  private Label imageLabel;
  private boolean imageLoaded = false;
  private ScrolledComposite scrolledComposite;
  private ImageData image;
  private Text suggestions;
  private Job job;
  private Button check;
  

  @Inject public CheckView(final Composite parent) {
    this.parent = parent;
    scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.BORDER);
    imageLabel = new Label(scrolledComposite, SWT.BORDER | SWT.CENTER);
    scrolledComposite.setContent(imageLabel);
    scrolledComposite.setExpandVertical(true);
    scrolledComposite.setExpandHorizontal(true);
    // scrolledComposite.setMinSize(imageLabel.computeSize(SWT.MAX, SWT.MAX));
    scrolledComposite.setMinSize(new Point(900, 1440)); // IMG_SIZE
    addSuggestions();
    GridLayoutFactory.fillDefaults().generateLayout(parent);
  }

  private void addSuggestions() {
    Composite bottom = new Composite(parent, SWT.NONE);
    GridLayout layout = new GridLayout(2, false);
    bottom.setLayout(layout);
    check = new Button(bottom, SWT.CHECK);
    check.setToolTipText("Suggest corrections");
    check.setSelection(false);
    suggestions = new Text(bottom, SWT.WRAP);
    suggestions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
  }

  @Inject public void setSelection(
      @Optional @Named( IServiceConstants.ACTIVE_SELECTION ) final List<Page> pages) {
    if (pages != null && pages.size() > 0) {
      Page page = pages.get(0);
      try {
        updateImage(page);
      } catch (MalformedURLException e) {
        handle(e);
      } catch (IOException e) {
        handle(e);
      }
    } else {
      return;
    }
  }
  
  private void handle(Exception e) {
    MessageDialog.openError(parent.getShell(), "Could not load scan",
        "Could not load the image file for the current page");
    e.printStackTrace();
  }

  @Inject public void setSelection(@Optional @Named( IServiceConstants.ACTIVE_SELECTION ) final Text word) {
    if (imageLoaded && word != null) {
      markPosition(word);
      if (job != null) {
        /* If a word is selected while we had a Job running for the previous word, cancel that: */
        job.cancel();
      }
      if (word == null) {
        suggestions.setText("No word selected");
      } else if (!check.getSelection()) {
        suggestions.setText("Edit suggestions disabled");
      } else {
        findEditSuggestions((Word)word.getData());
        job.setPriority(Job.DECORATE);
        job.schedule();
      }
    }
  }
  
  private void findEditSuggestions(final Word word) {
    suggestions.setText("Finding edit suggestions...");
    job = new Job("Edit suggestions search job") {
      protected IStatus run(final IProgressMonitor monitor) {
        final boolean complete = word.prepSuggestions();
        suggestions.getDisplay().asyncExec(new Runnable() {
          @Override
          public void run() {
            if (!complete) {
              suggestions.setText("Finding edit suggestions...");
            } else {
              final String s = "Suggestions for " + word.original() + ": "
                  + word.suggestions().mkString(", ");
              if (!suggestions.isDisposed()) {
                suggestions.setText(s);
              }
            }
          }
        });
        return Status.OK_STATUS;
      }

      @Override
      protected void canceling() {
        word.cancelled_$eq(true);
        suggestions.setText("Finding edit suggestions...");
      };
    };
  }

  private void updateImage(final Page page) throws IOException {
    Image loadedImage = loadImage(page);
    image = loadedImage.getImageData();
    imageLabel.setImage(loadedImage);
    imageLoaded = true;
  }

  private Image reloadImage() {
    Display display = parent.getDisplay();
    Image newImage = new Image(display, image);
    return newImage;
  }

  private Image loadImage(final Page page) throws IOException {
    Display display = parent.getDisplay();
    Image newImage = null;
    InputStream in = new ByteArrayInputStream(Index.loadImageFor(page)); // TODO image as lazy def in page, fetched on demand?
    newImage = new Image(display, in); //new ZipFile(new File(page.zip().get().getName())).getInputStream(page.image().get()));
    return newImage;
  }

  private void markPosition(final Text text) {
    imageLabel.getImage().dispose();
    Word word = (Word) text.getData();
    Box box = word.position();
    Rectangle rect = new Rectangle(box.x() - 10, box.y() - 4, box.width() + 20, box.height() + 12); // IMG_SIZE
    System.out.println("Current word: " + word);
    Image image = reloadImage();
    GC gc = new GC(image);
    drawBoxArea(rect, gc);
    drawBoxBorder(rect, gc);
    imageLabel.setImage(image);
    gc.dispose();
    scrolledComposite.setOrigin(new Point(rect.x - 10, rect.y - 10)); // IMG_SIZE
  }

  private void drawBoxBorder(final Rectangle rect, final GC gc) {
    gc.setAlpha(200);
    gc.setLineWidth(1);
    gc.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
    gc.drawRectangle(rect);
  }

  private void drawBoxArea(final Rectangle rect, final GC gc) {
    gc.setAlpha(50);
    gc.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_GREEN));
    gc.fillRectangle(rect);
  }

  private void clearMarker() {
    imageLabel.setImage(reloadImage());
  }

}
