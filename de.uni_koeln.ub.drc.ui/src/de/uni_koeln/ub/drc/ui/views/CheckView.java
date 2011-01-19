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
import java.util.ArrayList;
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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import de.uni_koeln.ub.drc.ui.DrcUiActivator;

/**
 * View containing the scanned page used to check the original word while
 * editing.
 * 
 * @author Fabian Steeg (fsteeg)
 */
public final class CheckView {
	private Composite parent;
	private Label imageLabel;
	private boolean imageLoaded = false;
	private ScrolledComposite scrolledComposite;
	private ImageData image;
	private Label suggestions;
	private Job job;
	private Button check;
	private Text word;
	private List<Button> suggestionButtons = new ArrayList<Button>();
	private Composite bottom;

	@Inject
	public CheckView(final Composite parent) {
		this.parent = parent;
		scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.BORDER);
		imageLabel = new Label(scrolledComposite, SWT.BORDER | SWT.CENTER);
		scrolledComposite.setContent(imageLabel);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		// scrolledComposite.setMinSize(imageLabel.computeSize(SWT.MAX,
		// SWT.MAX));
		scrolledComposite.setMinSize(new Point(900, 1440)); // IMG_SIZE
		addSuggestions();
		GridLayoutFactory.fillDefaults().generateLayout(parent);
	}

	@Inject
	public void setSelection(
			@Optional @Named(IServiceConstants.ACTIVE_SELECTION) final List<Page> pages) {
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

	@Inject
	public void setSelection(
			@Optional @Named(IServiceConstants.ACTIVE_SELECTION) final Text text) {
		Word word = null;
		if (imageLoaded && text != null
				&& (word = (Word) text.getData(Word.class.toString())) != null) {
			this.word = text;
			markPosition(text);
			if (job != null) {
				/*
				 * If a word is selected while we had a Job running for the
				 * previous word, cancel that:
				 */
				job.cancel();
			}
			if (text == null) {
				suggestions.setText("No word selected");
			} else if (!check.getSelection()) {
				suggestions.setText("Edit suggestions disabled");
				disposeButtons();
			} else if (word.isLocked()) {
				suggestions.setText("No edit suggestions - word is locked");
			} else {
				findEditSuggestions(word, text);
				job.setPriority(Job.DECORATE);
				job.schedule();
			}
		}
	}

	private void addSuggestions() {
		bottom = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(7, false);
		bottom.setLayout(layout);
		check = new Button(bottom, SWT.CHECK);
		check.setToolTipText("Suggest corrections");
		check.setSelection(false);
		check.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setSelection(word);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		suggestions = new Label(bottom, SWT.WRAP);
		suggestions.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	private void clearMarker() {
		imageLabel.setImage(reloadImage());
	}

	private void displaySuggestionButtons(final Word word, final Text text) {
		bottom.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				String words = word.suggestions().mkString(" ");
				for (String string : words.split(" ")) {
					final Button b = new Button(bottom, SWT.FLAT);
					b.setLayoutData(new GridData(SWT.NONE));
					b.setText(string);
					b.addSelectionListener(new SelectionListener() {

						@Override
						public void widgetSelected(SelectionEvent e) {
							if (text != null) {
								text.setText(b.getText());
								text.setSelection(text.getCaretPosition());
								text.setFocus();
							}
						}

						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
						}
					});
					suggestionButtons.add(b);
				}
				bottom.pack();
				bottom.redraw();
			}
		});
	}

	private void disposeButtons() {
		if (!suggestionButtons.isEmpty()) {
			for (Button b : suggestionButtons) {
				b.dispose();
			}
		}
		suggestionButtons = new ArrayList<Button>();
	}

	private void drawBoxArea(final Rectangle rect, final GC gc) {
		gc.setAlpha(50);
		gc.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_GREEN));
		gc.fillRectangle(rect);
	}

	private void drawBoxBorder(final Rectangle rect, final GC gc) {
		gc.setAlpha(200);
		gc.setLineWidth(1);
		gc.setForeground(parent.getDisplay().getSystemColor(
				SWT.COLOR_DARK_GREEN));
		gc.drawRectangle(rect);
	}

	private void findEditSuggestions(final Word word, final Text text) {
		disposeButtons();
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
							String info = (word.suggestions().size() == 0) ? "No reasonable edit suggestions found"
									: String.format(
											"Suggestions for %s (originally '%s'):",
											word.history().top().form(),
											word.original());
							
							if (!bottom.isDisposed())
								displaySuggestionButtons(word, text);

							if (!suggestions.isDisposed())
								suggestions.setText(info);
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

	private void handle(Exception e) {
		MessageDialog.openError(parent.getShell(), "Could not load scan",
				"Could not load the image file for the current page");
		e.printStackTrace();
	}

	private Image loadImage(final Page page) throws IOException {
		Display display = parent.getDisplay();
		Image newImage = null;
		// TODO image as lazy def in page, fetched on demand?
		InputStream in = new ByteArrayInputStream(Index.loadImageFor(
				DrcUiActivator.instance().db(), page));
		newImage = new Image(display, in);
		return newImage;
	}

	private void markPosition(final Text text) {
		imageLabel.getImage().dispose();
		Word word = (Word) text.getData(Word.class.toString());
		Box box = word.position();
		Rectangle rect = new Rectangle(box.x() - 10, box.y() - 4,
				box.width() + 20, box.height() + 12); // IMG_SIZE
		Image image = reloadImage();
		GC gc = new GC(image);
		drawBoxArea(rect, gc);
		drawBoxBorder(rect, gc);
		imageLabel.setImage(image);
		gc.dispose();
		scrolledComposite.setOrigin(new Point(rect.x - 10, rect.y - 10)); // IMG_SIZE
	}

	private Image reloadImage() {
		Display display = parent.getDisplay();
		Image newImage = new Image(display, image);
		return newImage;
	}

	private void updateImage(final Page page) throws IOException {
		if (imageLabel != null && imageLabel.getImage() != null
				&& !imageLabel.getImage().isDisposed())
			imageLabel.getImage().dispose();
		Image loadedImage = loadImage(page);
		image = loadedImage.getImageData();
		imageLabel.setImage(loadedImage);
		imageLoaded = true;
	}

}
